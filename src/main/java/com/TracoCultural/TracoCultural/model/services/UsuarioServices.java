package com.TracoCultural.TracoCultural.model.services;

import com.TracoCultural.TracoCultural.model.Repository.ComentarioRepository;
import com.TracoCultural.TracoCultural.model.Repository.EventoRepository;
import com.TracoCultural.TracoCultural.model.Repository.FavoritoRepository;
import com.TracoCultural.TracoCultural.model.Repository.NotificacaoRepository;
import com.TracoCultural.TracoCultural.model.Repository.UsuarioRepository;
import com.TracoCultural.TracoCultural.model.entity.Evento;
import com.TracoCultural.TracoCultural.model.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class UsuarioServices {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private FavoritoRepository favoritoRepository;

    @Autowired
    private NotificacaoRepository notificacaoRepository;


    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Usuario findById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario não encontrado com o ID: " + id));
    }

    public Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Usuario save(Usuario usuario) {
        if (usuario.getNome() == null || usuario.getNome().isBlank())
            throw new IllegalArgumentException("Nome é obrigatório");

        if (usuario.getEmail() == null || !usuario.getEmail().matches("^[\\w.+\\-]+@[\\w\\-]+\\.[a-zA-Z]{2,}$"))
            throw new IllegalArgumentException("E-mail inválido");

        if (usuario.getSenha() == null || usuario.getSenha().length() < 8)
            throw new IllegalArgumentException("Senha deve ter no mínimo 8 caracteres");

        if (usuarioRepository.findByEmail(usuario.getEmail()) != null)
            throw new IllegalStateException("Email já cadastrado");

        usuario.setIsAdm(false);
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        return usuarioRepository.save(usuario);
    }

    public Usuario update(Long id, Usuario usuario) {
        Usuario existente = findById(id);
        existente.setNome(usuario.getNome());

        Usuario comMesmoEmail = usuarioRepository.findByEmail(usuario.getEmail());
        if (comMesmoEmail != null && !comMesmoEmail.getId().equals(id))
            throw new RuntimeException("EMAIL_EM_USO");
        existente.setEmail(usuario.getEmail());

        if (usuario.getSenha() != null && !usuario.getSenha().isBlank())
            existente.setSenha(passwordEncoder.encode(usuario.getSenha()));
        if (usuario.getIcone() != null)    existente.setIcone(usuario.getIcone());
        if (usuario.getCorFundo() != null) existente.setCorFundo(usuario.getCorFundo());
        if (usuario.getEstado() != null)   existente.setEstado(usuario.getEstado());
        return usuarioRepository.save(existente);
    }

    public void atualizarSenha(Long id, String senhaAtual, String novaSenha) {
        Usuario existente = findById(id);

        if (!passwordEncoder.matches(senhaAtual, existente.getSenha()))
            throw new IllegalArgumentException("Senha atual incorreta.");

        if (novaSenha == null || novaSenha.length() < 8)
            throw new IllegalArgumentException("Nova senha deve ter no mínimo 8 caracteres.");

        existente.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(existente);
    }

    public boolean idExists(String id) {
        return usuarioRepository.existsById(Long.parseLong(id));
    }

    @Transactional
    public ResponseEntity<Object> deleteById(String id) {
        try {
            Long uid = Long.parseLong(id);
            if (!usuarioRepository.existsById(uid))
                return ResponseEntity.status(404).body(
                        Map.of("status", 404, "message", "Usuário não encontrado"));

            // Ordem importa: tudo que referencia o usuário (ou os eventos dele)
            // precisa sumir ANTES da linha do usuário/evento em si, senão o
            // banco recusa por causa das chaves estrangeiras.

            // 1. Favoritos QUE ESSE USUÁRIO fez (em qualquer evento, não só nos dele)
            favoritoRepository.deleteByUsuarioId(uid);
            // 2. Notificações desse usuário
            notificacaoRepository.deleteByIdUsuarioFk(uid);
            // 3. Comentários QUE ESSE USUÁRIO escreveu em eventos de outras pessoas
            comentarioRepository.deleteByIdUsuarioFk(uid);

            // 4. Para cada evento que esse usuário CRIOU: limpar quem
            //    interagiu com ele (favoritos, comentários, notificações)
            List<Evento> eventos = eventoRepository.findByIdUsuarioFk(uid);
            for (Evento ev : eventos) {
                favoritoRepository.deleteByEventoId(ev.getId());
                comentarioRepository.deleteByIdEventoFk(ev.getId());
                notificacaoRepository.deleteByIdEventoFk(ev.getId());
            }
            eventoRepository.deleteByIdUsuarioFk(uid);

            usuarioRepository.deleteById(uid);
            return ResponseEntity.ok(Map.of("status", 200, "message", "Conta excluída com sucesso"));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "message", "ID inválido"));
        }
    }
}