package com.TracoCultural.TracoCultural.model.services;

import com.TracoCultural.TracoCultural.model.Repository.ComentarioRepository;
import com.TracoCultural.TracoCultural.model.Repository.EventoRepository;
import com.TracoCultural.TracoCultural.model.Repository.UsuarioRepository;
import com.TracoCultural.TracoCultural.model.entity.Comentario;
import com.TracoCultural.TracoCultural.model.entity.Evento;
import com.TracoCultural.TracoCultural.model.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ComentarioService {

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private NotificacaoService notificacaoService;

    public List<Comentario> listarPorEvento(Long eventoId) {
        List<Comentario> comentarios = comentarioRepository.findByIdEventoFkOrderByDataCriacaoDesc(eventoId);

        // Resolve o nome de cada autor em memória (não persistido na tabela Comentario)
        Map<Long, String> cacheNomes = new HashMap<>();
        for (Comentario c : comentarios) {
            String nome = cacheNomes.computeIfAbsent(c.getIdUsuarioFk(), id -> {
                Usuario u = usuarioRepository.findById(id).orElse(null);
                return u != null ? u.getNome() : "Usuário removido";
            });
            c.setNomeUsuario(nome);
        }

        return comentarios;
    }

    public Comentario salvar(Long eventoId, String texto, Authentication auth) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Texto do comentário não pode ser vazio");
        }
        if (texto.length() > 500) {
            throw new IllegalArgumentException("Texto do comentário deve ter no máximo 500 caracteres");
        }

        Usuario usuario = usuarioRepository.findByEmail(auth.getName());
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário autenticado não encontrado");
        }

        // Evita comentário órfão apontando pra um evento que não existe
        // (ex: id errado, ou evento apagado bem no meio da requisição).
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new IllegalArgumentException("Evento não encontrado com o ID: " + eventoId));

        Comentario comentario = new Comentario();
        comentario.setTexto(texto.trim());
        comentario.setIdEventoFk(eventoId);
        comentario.setIdUsuarioFk(usuario.getId());

        Comentario salvo = comentarioRepository.save(comentario);
        salvo.setNomeUsuario(usuario.getNome()); // só em memória, para já devolver no response

        // Notifica o dono do evento, exceto se ele mesmo estiver comentando.
        // Isolado num try/catch: se a criação da notificação falhar por
        // qualquer motivo, o comentário (que já foi salvo com sucesso) não
        // pode virar um erro 500 pro usuário -- ele só não recebe o aviso.
        try {
            if (evento.getIdUsuarioFk() != null && !evento.getIdUsuarioFk().equals(usuario.getId())) {
                notificacaoService.criar(
                        evento.getIdUsuarioFk(),
                        eventoId,
                        "COMENTARIO",
                        usuario.getNome() + " comentou no seu evento \"" + evento.getNome() + "\""
                );
            }
        } catch (RuntimeException ignored) {
            // Comentário já está salvo; falha aqui não deve propagar como erro.
        }

        return salvo;
    }

    public void deletar(Long comentarioId, Authentication auth) {
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new IllegalArgumentException("Comentário não encontrado"));

        Usuario usuario = usuarioRepository.findByEmail(auth.getName());
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário autenticado não encontrado");
        }

        boolean ehDono = comentario.getIdUsuarioFk().equals(usuario.getId());
        boolean ehAdmin = usuario.getIsAdm();

        if (!ehDono && !ehAdmin) {
            throw new SecurityException("SEM_PERMISSAO");
        }

        comentarioRepository.deleteById(comentarioId);
    }
}