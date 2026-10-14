package com.biblioteca.controller;

import com.biblioteca.model.Autor;
import com.biblioteca.service.AutorService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
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
@RequestMapping("/autores")
public class AutorController {

    private final AutorService autorService;

    public AutorController(AutorService autorService) {
        this.autorService = autorService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String erro, Model model) {
        model.addAttribute("autores", autorService.listarTodos());
        model.addAttribute("erro", erro);
        return "autores/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("autor", new Autor());
        return "autores/form";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("autor") Autor autor, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "autores/form";
        }
        autorService.salvar(autor);
        return "redirect:/autores";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("autor", autorService.buscarPorId(id));
        return "autores/form";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id) {
        try {
            autorService.excluir(id);
            return "redirect:/autores";
        } catch (DataIntegrityViolationException ex) {
            return "redirect:/autores?erro=autor-em-uso";
        }
    }

    @GetMapping("/{id}")
    public String detalhes(@PathVariable Long id, Model model) {
        model.addAttribute("autor", autorService.buscarPorId(id));
        return "autores/detalhes";
    }
}
