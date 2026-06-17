package live.nxasenpai.NxaSenpai.controller;

import jakarta.validation.Valid;
import live.nxasenpai.NxaSenpai.model.Template;
import live.nxasenpai.NxaSenpai.service.TemplateService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/templates")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    // ── List ──

    @GetMapping
    public String listTemplates(Model model, @RequestParam(required = false) String search) {
        if (search != null && !search.isBlank()) {
            model.addAttribute("templates", templateService.searchTemplates(search));
        } else {
            model.addAttribute("templates", templateService.getAllTemplates());
        }
        return "template/list";
    }

    // ── View ──

    @GetMapping("/{id}")
    public String viewTemplate(@PathVariable Long id, Model model) {
        Template template = templateService.getTemplateById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found"));
        model.addAttribute("template", template);
        return "template/view";
    }

    // ── Create form ──

    @GetMapping("/new")
    public String newTemplateForm(Model model) {
        model.addAttribute("template", new Template());
        return "template/form";
    }

    // ── Create ──

    @PostMapping
    public String createTemplate(@Valid @ModelAttribute Template template,
                                 BindingResult result) {
        if (result.hasErrors()) {
            return "template/form";
        }
        templateService.createTemplate(template);
        return "redirect:/templates";
    }

    // ── Edit form ──

    @GetMapping("/{id}/edit")
    public String editTemplateForm(@PathVariable Long id, Model model) {
        Template template = templateService.getTemplateById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found"));
        model.addAttribute("template", template);
        return "template/form";
    }

    // ── Update ──

    @PostMapping("/{id}")
    public String updateTemplate(@PathVariable Long id,
                                 @Valid @ModelAttribute Template template,
                                 BindingResult result) {
        if (result.hasErrors()) {
            return "template/form";
        }
        templateService.updateTemplate(id, template);
        return "redirect:/templates/" + id;
    }

    // ── Delete ──

    @PostMapping("/{id}/delete")
    public String deleteTemplate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            templateService.deleteTemplate(id);
            redirectAttributes.addFlashAttribute("message", "Template deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete template: " + e.getMessage());
        }
        return "redirect:/templates";
    }
}
