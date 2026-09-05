package com.TracoCultural.TracoCultural.model.services;

import com.TracoCultural.TracoCultural.model.Repository.ComentarioRepository;
import com.TracoCultural.TracoCultural.model.Repository.EventoRepository;
import com.TracoCultural.TracoCultural.model.Repository.FavoritoRepository;
import com.TracoCultural.TracoCultural.model.Repository.NotificacaoRepository;
import com.TracoCultural.TracoCultural.model.Repository.UsuarioRepository;
import com.TracoCultural.TracoCultural.model.dto.PaginaEventosDTO;
import com.TracoCultural.TracoCultural.model.entity.Evento;
import com.TracoCultural.TracoCultural.model.entity.Usuario;
import com.TracoCultural.TracoCultural.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventoService {

    private static final Logger logger = LoggerFactory.getLogger(EventoService.class);
    private static final int LIMITE_IMAGEM_BYTES = 2 * 1024 * 1024; // 2MB
    private static final long TRES_DIAS_MS = 3L * 24 * 60 * 60 * 1000;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private FavoritoRepository favoritoRepository;

    @Autowired
    private NotificacaoRepository notificacaoRepository;


    public List<Evento> findAll() {
        return eventoRepository.findAll();
    }

    public List<Evento> findByCidade(String cidade) {
        return eventoRepository.findByCidadeIgnoreCase(cidade);
    }

    public List<Evento> findByCategoria(Long categoriaId) {
        return eventoRepository.findByCategoriaId(categoriaId);
    }

    public List<Evento> findByCidadeAndCategoria(String cidade, Long categoriaId) {
        return eventoRepository.findByCidadeIgnoreCaseAndCategoriaId(cidade, categoriaId);
    }

    public Evento findById(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado com o ID: " + id));
    }

    public Evento save(Evento evento) {
        if (evento.getCardImage() != null && evento.getCardImage().length > LIMITE_IMAGEM_BYTES)
            throw new IllegalArgumentException("Imagem deve ter no máximo 2MB.");

        if (evento.getDataFim() != null && !evento.getDataFim().after(evento.getDataInicio()))
            throw new IllegalArgumentException("Data de término deve ser posterior à data de início.");

        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario usuario = usuarioRepository.findByEmail(email);
        evento.setIdUsuarioFk(usuario.getId());

        return eventoRepository.save(evento);
    }

    public Evento update(Long id, Evento evento) {
        Evento existente = findById(id);

        if (evento.getCardImage() != null && evento.getCardImage().length > LIMITE_IMAGEM_BYTES)
            throw new IllegalArgumentException("Imagem deve ter no máximo 2MB.");

        Date dataInicioFinal = evento.getDataInicio() != null ? evento.getDataInicio() : existente.getDataInicio();
        Date dataFimFinal = evento.getDataFim() != null ? evento.getDataFim() : existente.getDataFim();

        if (dataFimFinal != null && !dataFimFinal.after(dataInicioFinal))
            throw new IllegalArgumentException("Data de término deve ser posterior à data de início.");
 
        if (evento.getNome() != null && !evento.getNome().isBlank()) existente.setNome(evento.getNome());
        existente.setDescricao(evento.getDescricao());
        existente.setDataInicio(dataInicioFinal);
        existente.setDataFim(dataFimFinal);
        if (evento.getCidade() != null && !evento.getCidade().isBlank()) existente.setCidade(evento.getCidade());
        existente.setLinkExterno(evento.getLinkExterno());
        if (evento.getCategoria() != null) existente.setCategoria(evento.getCategoria());
        if (evento.getCardImage() != null) existente.setCardImage(evento.getCardImage());
        return eventoRepository.save(existente);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!eventoRepository.existsById(id))
            throw new RuntimeException("Evento não encontrado com o ID: " + id);
        comentarioRepository.deleteByIdEventoFk(id);
        favoritoRepository.deleteByEventoId(id);
        notificacaoRepository.deleteByIdEventoFk(id);
        eventoRepository.deleteById(id);
    }

    public List<Evento> findByUsuarioId(Long id) {
        return eventoRepository.findByIdUsuarioFk(id);
    }

    /**
     * Busca por texto livre (nome, cidade, descrição — ignorando acento/caixa)
     * com paginação em memória. Base ainda pequena o suficiente pra isso ser
     * seguro; se a tabela crescer muito, trocar por uma query nativa com
     * COLLATE accent-insensitive é o próximo passo.
     */
    public PaginaEventosDTO buscarPaginado(String q, Long categoriaId, String cidade, int page, int size) {
        List<Evento> base;
        if (cidade != null && categoriaId != null) {
            base = eventoRepository.findByCidadeIgnoreCaseAndCategoriaId(cidade, categoriaId);
        } else if (cidade != null) {
            base = eventoRepository.findByCidadeIgnoreCase(cidade);
        } else if (categoriaId != null) {
            base = eventoRepository.findByCategoriaId(categoriaId);
        } else {
            base = eventoRepository.findAll();
        }

        List<Evento> filtrados = base.stream()
                .filter(e -> TextUtils.contains(e.getNome(), q)
                        || TextUtils.contains(e.getCidade(), q)
                        || TextUtils.contains(e.getDescricao(), q))
                .collect(Collectors.toList());

        int from = Math.min(page * size, filtrados.size());
        int to = Math.min(from + size, filtrados.size());
        List<Evento> pagina = filtrados.subList(from, to);

        return new PaginaEventosDTO(pagina, page, size, filtrados.size());
    }

    /**
     * Roda todo dia às 3h30 (fora do horário de pico): apaga eventos
     * encerrados há mais de 3 dias. "Encerrado" = passou da dataFim (ou da
     * dataInicio, se o evento não tiver dataFim -- evento de um dia só).
     * Reaproveita deleteById, que já limpa favoritos/comentários/
     * notificações relacionadas com segurança.
     */
    @Scheduled(cron = "0 30 3 * * *")
    public void limparEventosEncerrados() {
        Date agora = new Date();
        Date limite = new Date(agora.getTime() - TRES_DIAS_MS);

        List<Evento> todos = eventoRepository.findAll();
        for (Evento evento : todos) {
            Date dataReferencia = evento.getDataFim() != null ? evento.getDataFim() : evento.getDataInicio();
            if (dataReferencia == null) continue;

            boolean encerradoHaMaisDeTresDias = dataReferencia.before(limite);
            if (!encerradoHaMaisDeTresDias) continue;

            try {
                deleteById(evento.getId());
                logger.info("Evento '{}' (id={}) removido automaticamente — encerrado desde {}",
                        evento.getNome(), evento.getId(), dataReferencia);
            } catch (RuntimeException e) {
                logger.error("Falha ao remover automaticamente o evento id={}: {}", evento.getId(), e.getMessage());
            }
        }
    }
}