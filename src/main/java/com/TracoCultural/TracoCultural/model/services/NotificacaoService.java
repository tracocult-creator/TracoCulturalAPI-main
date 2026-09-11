package com.TracoCultural.TracoCultural.model.services;

import com.TracoCultural.TracoCultural.model.Repository.EnvioNotificacaoRepository;
import com.TracoCultural.TracoCultural.model.Repository.EventoRepository;
import com.TracoCultural.TracoCultural.model.Repository.FavoritoRepository;
import com.TracoCultural.TracoCultural.model.Repository.NotificacaoRepository;
import com.TracoCultural.TracoCultural.model.Repository.UsuarioRepository;
import com.TracoCultural.TracoCultural.model.dto.EnvioNotificacaoDTO;
import com.TracoCultural.TracoCultural.model.entity.EnvioNotificacao;
import com.TracoCultural.TracoCultural.model.entity.Evento;
import com.TracoCultural.TracoCultural.model.entity.Favorito;
import com.TracoCultural.TracoCultural.model.entity.Notificacao;
import com.TracoCultural.TracoCultural.model.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Service
public class NotificacaoService {

    private static final long UM_DIA_MS = 24L * 60 * 60 * 1000;

    @Autowired
    private NotificacaoRepository notificacaoRepository;
    @Autowired
    private EnvioNotificacaoRepository envioNotificacaoRepository;
    @Autowired
    private FavoritoRepository favoritoRepository;
    @Autowired
    private EventoRepository EventoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Notificacao> listar(Long usuarioId) {
        return notificacaoRepository.findByIdUsuarioFkOrderByDataCriacaoDesc(usuarioId);
    }

    public long contarNaoLidas(Long usuarioId) {
        return notificacaoRepository.countByIdUsuarioFkAndLidaFalse(usuarioId);
    }

    public void marcarComoLida(Long id, Long usuarioId) {
        Notificacao n = notificacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificação não encontrada"));
        if (!n.getIdUsuarioFk().equals(usuarioId)) {
            throw new SecurityException("SEM_PERMISSAO");
        }
        n.setLida(true);
        notificacaoRepository.save(n);
    }

    public void marcarTodasComoLidas(Long usuarioId) {
        List<Notificacao> naoLidas = notificacaoRepository.findByIdUsuarioFkOrderByDataCriacaoDesc(usuarioId)
                .stream().filter(n -> !n.isLida()).toList();
        naoLidas.forEach(n -> n.setLida(true));
        notificacaoRepository.saveAll(naoLidas);
    }

    public void criar(Long usuarioDestinoId, Long eventoId, String tipo, String mensagem) {
        Notificacao n = new Notificacao();
        n.setIdUsuarioFk(usuarioDestinoId);
        n.setIdEventoFk(eventoId);
        n.setTipo(tipo);
        n.setMensagem(mensagem);
        notificacaoRepository.save(n);
    }
    public EnvioNotificacaoDTO enviarGeral(String mensagem, Long remetenteId) {
        List<Long> destinatarios = usuarioRepository.findAll().stream()
                .map(Usuario::getId)
                .toList();

        EnvioNotificacao envio = new EnvioNotificacao();
        envio.setIdUsuarioRemetenteFk(remetenteId);
        envio.setIdEventoFk(null);
        envio.setTipo("GERAL");
        envio.setMensagem(mensagem);
        envio.setTotalDestinatarios(destinatarios.size());
        envio = envioNotificacaoRepository.save(envio);

        criarNotificacoesDoEnvio(destinatarios, null, "GERAL", mensagem, envio.getId());

        return new EnvioNotificacaoDTO(envio, null);
    }

    public EnvioNotificacaoDTO notificarFavoritosDoEvento(Long eventoId, String mensagem, Long remetenteId) {
        List<Long> destinatarios = favoritoRepository.findByEventoId(eventoId).stream()
                .map(f -> f.getUsuario().getId())
                .distinct()
                .toList();

        EnvioNotificacao envio = new EnvioNotificacao();
        envio.setIdUsuarioRemetenteFk(remetenteId);
        envio.setIdEventoFk(eventoId);
        envio.setTipo("EVENTO_ATUALIZACAO");
        envio.setMensagem(mensagem);
        envio.setTotalDestinatarios(destinatarios.size());
        envio = envioNotificacaoRepository.save(envio);

        criarNotificacoesDoEnvio(destinatarios, eventoId, "EVENTO_ATUALIZACAO", mensagem, envio.getId());

        String nomeEvento = EventoRepository.findById(eventoId).map(Evento::getNome).orElse(null);
        return new EnvioNotificacaoDTO(envio, nomeEvento);
    }

    private void criarNotificacoesDoEnvio(List<Long> destinatarios, Long eventoId, String tipo, String mensagem, Long envioId) {
        List<Notificacao> notificacoes = destinatarios.stream().map(usuarioId -> {
            Notificacao n = new Notificacao();
            n.setIdUsuarioFk(usuarioId);
            n.setIdEventoFk(eventoId);
            n.setTipo(tipo);
            n.setMensagem(mensagem);
            n.setIdEnvioFk(envioId);
            return n;
        }).toList();
        notificacaoRepository.saveAll(notificacoes);
    }

    /**
     * Lista os envios em lote visiveis pro usuario autenticado: os que ele
     * mesmo mandou (avisos de eventos que criou) e, se for admin, tambem os
     * avisos gerais da plataforma (mandados por qualquer admin).
     */
    public List<EnvioNotificacaoDTO> listarEnvios(Usuario usuario) {
        List<EnvioNotificacao> envios = new ArrayList<>(
                envioNotificacaoRepository.findByIdUsuarioRemetenteFkOrderByDataCriacaoDesc(usuario.getId()));

        if (usuario.getIsAdm()) {
            envioNotificacaoRepository.findByTipoOrderByDataCriacaoDesc("GERAL").forEach(e -> {
                if (envios.stream().noneMatch(existente -> existente.getId().equals(e.getId()))) {
                    envios.add(e);
                }
            });
        }

        envios.sort(Comparator.comparing(EnvioNotificacao::getDataCriacao).reversed());

        return envios.stream().map(envio -> {
            String nomeEvento = envio.getIdEventoFk() == null ? null :
                    EventoRepository.findById(envio.getIdEventoFk()).map(Evento::getNome).orElse(null);
            return new EnvioNotificacaoDTO(envio, nomeEvento);
        }).toList();
    }

    private EnvioNotificacao buscarEnvioComPermissao(Long envioId, Usuario usuario) {
        EnvioNotificacao envio = envioNotificacaoRepository.findById(envioId)
                .orElseThrow(() -> new RuntimeException("Envio nao encontrado"));

        boolean ehRemetente = envio.getIdUsuarioRemetenteFk() != null
                && envio.getIdUsuarioRemetenteFk().equals(usuario.getId());
        if (!ehRemetente && !usuario.getIsAdm()) {
            throw new SecurityException("SEM_PERMISSAO");
        }
        return envio;
    }

    public EnvioNotificacaoDTO editarEnvio(Long envioId, String novaMensagem, Usuario usuario) {
        EnvioNotificacao envio = buscarEnvioComPermissao(envioId, usuario);

        envio.setMensagem(novaMensagem);
        envio.setDataAtualizacao(LocalDateTime.now());
        envio = envioNotificacaoRepository.save(envio);

        // reflete a edicao nas notificacoes individuais ja criadas
        List<Notificacao> notificacoes = notificacaoRepository.findByIdEnvioFk(envioId);
        notificacoes.forEach(n -> n.setMensagem(novaMensagem));
        notificacaoRepository.saveAll(notificacoes);

        String nomeEvento = envio.getIdEventoFk() == null ? null :
                EventoRepository.findById(envio.getIdEventoFk()).map(Evento::getNome).orElse(null);
        return new EnvioNotificacaoDTO(envio, nomeEvento);
    }

    public void excluirEnvio(Long envioId, Usuario usuario) {
        EnvioNotificacao envio = buscarEnvioComPermissao(envioId, usuario);
        notificacaoRepository.deleteByIdEnvioFk(envio.getId());
        envioNotificacaoRepository.delete(envio);
    }


     
    @Scheduled(cron = "0 0 9 * * *")
    public void avisarEventosProximos() {
        Date agora = new Date();
        Date em24h = new Date(agora.getTime() + UM_DIA_MS);

        List<Favorito> favoritos = favoritoRepository.findAll();
        for (Favorito favorito : favoritos) {
            Evento evento = favorito.getEvento();
            if (evento == null || evento.getDataInicio() == null) continue;

            boolean comecaEmBreve = evento.getDataInicio().after(agora) && evento.getDataInicio().before(em24h);
            if (!comecaEmBreve) continue;

            Long usuarioId = favorito.getUsuario().getId();
            boolean jaAvisado = notificacaoRepository.existsByIdUsuarioFkAndIdEventoFkAndTipo(
                    usuarioId, evento.getId(), "EVENTO_PROXIMO");
            if (jaAvisado) continue;

            criar(usuarioId, evento.getId(), "EVENTO_PROXIMO",
                    "\"" + evento.getNome() + "\" começa em breve — não esquece de conferir!");
        }
    }
}