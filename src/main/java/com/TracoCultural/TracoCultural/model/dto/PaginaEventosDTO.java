package com.TracoCultural.TracoCultural.model.dto;

import com.TracoCultural.TracoCultural.model.entity.Evento;

import java.util.List;

/**
 * Resposta paginada de eventos. Só é usada quando o cliente manda
 * "q" (busca livre) e/ou "page"/"size" na query string — sem esses
 * parâmetros, GET /eventos continua devolvendo a lista simples de
 * sempre, pra não quebrar o app mobile.
 */
public class PaginaEventosDTO {

    private List<Evento> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public PaginaEventosDTO(List<Evento> content, int page, int size, long totalElements) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    }

    public List<Evento> getContent() { return content; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
}
