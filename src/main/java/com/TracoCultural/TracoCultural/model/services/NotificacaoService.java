package com.TracoCultural.TracoCultural.model.services;

import com.TracoCultural.TracoCultural.model.Repository.EventoRepository;
import com.TracoCultural.TracoCultural.model.Repository.FavoritoRepository;
import com.TracoCultural.TracoCultural.model.Repository.NotificacaoRepository;
import com.TracoCultural.TracoCultural.model.entity.Evento;
import com.TracoCultural.TracoCultural.model.entity.Favorito;
import com.TracoCultural.TracoCultural.model.entity.Notificacao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class NotificacaoService {

    private static final long UM_DIA_MS = 24L * 60 * 60 * 1000;

    @Autowired
    private NotificacaoRepository notificacaoRepository;
    @Autowired
    private FavoritoRepository favoritoRepository;
    @Autowired
    private EventoRepository EventoRepository;

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

    /**
     * Roda todo dia às 9h: avisa quem favoritou um evento que começa nas
     * próximas 24h. Cada usuário só recebe um aviso por evento (checa se já
     * existe notificação do tipo EVENTO_PROXIMO pra esse par usuário/evento
     * antes de criar outra).
     */
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
