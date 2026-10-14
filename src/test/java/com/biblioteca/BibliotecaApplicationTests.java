package com.biblioteca;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.biblioteca.model.Autor;
import com.biblioteca.model.Livro;
import com.biblioteca.repository.AutorRepository;
import com.biblioteca.repository.LivroRepository;
import com.biblioteca.service.AutorService;
import com.biblioteca.service.LivroService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class BibliotecaApplicationTests {

    @Autowired
    private AutorService autorService;

    @Autowired
    private LivroService livroService;

    @Autowired
    private AutorRepository autorRepository;

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void carregaCatalogoInicialComAutoresDosLivros() {
        assertThat(autorService.listarTodos()).extracting(Autor::getNome)
                .containsExactlyInAnyOrder("Machado de Assis", "Clarice Lispector", "George Orwell");
        assertThat(livroRepository.count()).isEqualTo(5);
        assertThat(livroService.buscarPorId(1L).getAutor().getNome()).isEqualTo("Machado de Assis");
        assertThat(livroService.buscarPorId(3L).getAutor().getNome()).isEqualTo("Clarice Lispector");
        assertThat(livroService.buscarPorId(5L).getAutor().getNome()).isEqualTo("George Orwell");
    }

    @Test
    void buscaParteDoTituloIgnorandoMaiusculasEEspacosExternos() {
        Page<Livro> resultado = livroService.listar("  cAsMuRrO  ", 0, 10);

        assertThat(resultado.getContent()).extracting(Livro::getTitulo).containsExactly("Dom Casmurro");
        assertThat(resultado.getTotalElements()).isEqualTo(1);
    }

    @Test
    void listaCatalogoOrdenadoComPaginacaoEContagemTotal() {
        Page<Livro> primeiraPagina = livroService.listar(null, 0, 2);
        Page<Livro> segundaPagina = livroService.listar("   ", 1, 2);
        Page<Livro> ultimaPagina = livroService.listar("", 2, 2);

        assertThat(primeiraPagina.getContent()).extracting(Livro::getTitulo)
                .containsExactly("1984", "A Hora da Estrela");
        assertThat(segundaPagina.getContent()).extracting(Livro::getTitulo)
                .containsExactly("A Revolução dos Bichos", "Dom Casmurro");
        assertThat(ultimaPagina.getContent()).extracting(Livro::getTitulo)
                .containsExactly("Memórias Póstumas de Brás Cubas");
        assertThat(primeiraPagina.getTotalElements()).isEqualTo(5);
        assertThat(primeiraPagina.getTotalPages()).isEqualTo(3);
        assertThat(ultimaPagina.isLast()).isTrue();
    }

    @Test
    void paginaAlemDoCatalogoFicaVaziaSemPerderContagemTotal() {
        Page<Livro> resultado = livroService.listar(null, 3, 2);

        assertThat(resultado.getContent()).isEmpty();
        assertThat(resultado.getTotalElements()).isEqualTo(5);
    }

    @Test
    void cadastraAutorELivroAposCargaInicialEPersisteRelacionamento() {
        Autor autor = new Autor();
        autor.setNome("Carolina Maria de Jesus");
        autor.setNacionalidade("Brasileira");
        autor = autorService.salvar(autor);

        Livro livro = new Livro();
        livro.setTitulo("Quarto de Despejo");
        livro.setIsbn("978-85-08-19055-4");
        livro.setAnoPublicacao(1960);
        livro.setAutor(autor);
        livro = livroService.salvar(livro);
        entityManager.flush();
        entityManager.clear();

        Livro livroPersistido = livroService.buscarPorId(livro.getId());
        assertThat(autor.getId()).isGreaterThan(3L);
        assertThat(livroPersistido.getId()).isGreaterThan(5L);
        assertThat(livroPersistido.getTitulo()).isEqualTo("Quarto de Despejo");
        assertThat(livroPersistido.getIsbn()).isEqualTo("978-85-08-19055-4");
        assertThat(livroPersistido.getAnoPublicacao()).isEqualTo(1960);
        assertThat(livroPersistido.getAutor().getId()).isEqualTo(autor.getId());
        assertThat(livroPersistido.getAutor().getNome()).isEqualTo("Carolina Maria de Jesus");
        assertThat(autorRepository.count()).isEqualTo(4);
        assertThat(livroRepository.count()).isEqualTo(6);
    }

    @Test
    void impedeIsbnDuplicadoMesmoEmAtualizacaoDeLivro() {
        Livro livro = livroService.buscarPorId(1L);
        livro.setIsbn(livroService.buscarPorId(2L).getIsbn());

        assertThatThrownBy(() -> livroRepository.saveAndFlush(livro))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void impedeExcluirAutorQuePossuiLivros() {
        autorService.excluir(1L);

        assertThatThrownBy(() -> autorRepository.flush())
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void informaQuandoLivroOuAutorNaoExiste() {
        assertThatThrownBy(() -> livroService.buscarPorId(Long.MAX_VALUE))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Livro não encontrado");
        assertThatThrownBy(() -> autorService.buscarPorId(Long.MAX_VALUE))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Autor não encontrado");
    }
}
