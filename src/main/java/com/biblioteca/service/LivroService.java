package com.biblioteca.service;

import com.biblioteca.model.Livro;
import com.biblioteca.repository.LivroRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class LivroService {

    private final LivroRepository livroRepository;

    public LivroService(LivroRepository livroRepository) {
        this.livroRepository = livroRepository;
    }

    public Page<Livro> listar(String titulo, int pagina, int tamanho) {
        Pageable pageable = PageRequest.of(pagina, tamanho);
        String filtro = titulo == null ? "" : titulo.trim();

        List<Livro> livros = livroRepository.findByTituloContainingIgnoreCase(filtro);
        livros.sort(Comparator.comparing(
                livro -> livro.getTitulo().toLowerCase(Locale.ROOT)
        ));

        int inicio = (int) pageable.getOffset();
        int fim = Math.min(inicio + pageable.getPageSize(), livros.size());
        List<Livro> conteudo = inicio >= livros.size() ? Collections.emptyList() : livros.subList(inicio, fim);

        return new PageImpl<>(conteudo, pageable, livros.size());
    }

    public Livro buscarPorId(Long id) {
        return livroRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Livro não encontrado"));
    }

    public Livro salvar(Livro livro) {
        return livroRepository.save(livro);
    }

    public void excluir(Long id) {
        livroRepository.deleteById(id);
    }
}
