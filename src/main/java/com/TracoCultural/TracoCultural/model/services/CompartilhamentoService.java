package com.TracoCultural.TracoCultural.model.services;

import com.TracoCultural.TracoCultural.model.Repository.CompartilhamentoRepository;
import com.TracoCultural.TracoCultural.model.Repository.EventoRepository;
import com.TracoCultural.TracoCultural.model.Repository.UsuarioRepository;
import com.TracoCultural.TracoCultural.model.entity.Compartilhamento;
import com.TracoCultural.TracoCultural.model.entity.Evento;
import com.TracoCultural.TracoCultural.model.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class CompartilhamentoService {

    @Autowired
    private CompartilhamentoRepository compartilhamentoRepository;
    @Autowired
    private EventoRepository eventoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    public long registrar(Long eventoId, String emailUsuario) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado com o ID: " + eventoId));
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario);

        Compartilhamento compartilhamento = new Compartilhamento();
        compartilhamento.setEvento(evento);
        compartilhamento.setUsuario(usuario);
        compartilhamento.setDataCompartilhamento(new Date());
        compartilhamentoRepository.save(compartilhamento);

        return contar(eventoId);
    }

    public long contar(Long eventoId) {
        return compartilhamentoRepository.countByEvento_Id(eventoId);
    }
}
