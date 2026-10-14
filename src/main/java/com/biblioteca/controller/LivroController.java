package com.biblioteca.controller;

import com.biblioteca.model.Livro;
import com.biblioteca.service.AutorService;
import com.biblioteca.service.LivroService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/livros")
public class LivroController {

    private final LivroService livroService;
    private final AutorService autorService;

    public LivroController(LivroService livroService, AutorService autorService) {
        this.livroService = livroService;
        this.autorService = autorService;
    }

    @GetMapping
    public String listar(
            @RequestParam(defaultValue = "") String titulo,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "5") int tamanho,
            Model model
    ) {
        Page<Livro> livros = livroService.listar(titulo, pagina, tamanho);
        model.addAttribute("livros", livros);
        model.addAttribute("titulo", titulo);
        model.addAttribute("pagina", pagina);
        return "livros/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("livro", new Livro());
        model.addAttribute("autores", autorService.listarTodos());
        return "livros/form";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("livro") Livro livro, BindingResult bindingResult, Model model) {
        Long autorId = livro.getAutor() != null ? livro.getAutor().getId() : null;
        if (autorId == null) {
            bindingResult.rejectValue("autor", "autor.obrigatorio", "Selecione um autor");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("autores", autorService.listarTodos());
            return "livros/form";
        }

        try {
            livro.setAutor(autorService.buscarPorId(autorId));
            livroService.salvar(livro);
            return "redirect:/livros";
        } catch (DataIntegrityViolationException ex) {
            bindingResult.rejectValue("isbn", "isbn.duplicado", "ISBN já cadastrado");
            model.addAttribute("autores", autorService.listarTodos());
            return "livros/form";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("livro", livroService.buscarPorId(id));
        model.addAttribute("autores", autorService.listarTodos());
        return "livros/form";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id) {
        livroService.excluir(id);
        return "redirect:/livros";
    }
}
