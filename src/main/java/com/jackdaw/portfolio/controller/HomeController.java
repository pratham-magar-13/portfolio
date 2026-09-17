package com.jackdaw.portfolio.controller;

import com.jackdaw.portfolio.model.ContactMessage;
import com.jackdaw.portfolio.repository.ContactMessageRepository;
import com.jackdaw.portfolio.web.ContactRateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Tag(name = "Portfolio", description = "Homepage rendering and contact-form submission")
@Controller
public class HomeController {

    private final ContactMessageRepository contactMessageRepository;
    private final ContactRateLimiter contactRateLimiter;

    public HomeController(
            ContactMessageRepository contactMessageRepository, ContactRateLimiter contactRateLimiter) {
        this.contactMessageRepository = contactMessageRepository;
        this.contactRateLimiter = contactRateLimiter;
    }

    @Operation(
            summary = "Render the portfolio homepage",
            description = "Returns the server-rendered single-page portfolio (Thymeleaf HTML).")
    @ApiResponse(responseCode = "200", description = "Homepage HTML rendered")
    @GetMapping("/")
    public String index(
            Model model,
            @Parameter(description = "When present, shows the contact success banner")
                    @RequestParam(value = "sent", required = false)
                    String sent,
            @Parameter(description = "When present, shows the rate-limit warning banner")
                    @RequestParam(value = "limited", required = false)
                    String limited) {
        if (!model.containsAttribute("contactMessage")) {
            model.addAttribute("contactMessage", new ContactMessage());
        }
        model.addAttribute("sent", sent != null);
        model.addAttribute("limited", limited != null);
        return "index";
    }

    @Operation(
            summary = "Submit a contact message",
            description =
                    "Accepts an HTML form submission, validates it, applies honeypot and "
                            + "rate-limit spam checks, persists the message, then redirects back to the "
                            + "contact section. Produces HTML redirects, not JSON.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "302",
                description =
                        "Redirect to /?sent=true#contact on success (or accepted honeypot), "
                                + "/#contact on validation errors, /?limited=true#contact when rate limited",
                content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/contact")
    public String submitContact(
            @ParameterObject @Valid @ModelAttribute("contactMessage") ContactMessage contactMessage,
            @Parameter(hidden = true) BindingResult bindingResult,
            @Parameter(
                            description = "Anti-spam honeypot field — must be left empty",
                            required = false)
                    @RequestParam(value = "website", required = false)
                    String website,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) RedirectAttributes redirectAttributes) {
        // Honeypot: bots fill the hidden "website" field; pretend success.
        if (website != null && !website.isBlank()) {
            return "redirect:/?sent=true#contact";
        }
        if (!contactRateLimiter.allow(clientIp(request))) {
            redirectAttributes.addFlashAttribute("contactMessage", contactMessage);
            return "redirect:/?limited=true#contact";
        }
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.contactMessage", bindingResult);
            redirectAttributes.addFlashAttribute("contactMessage", contactMessage);
            redirectAttributes.addFlashAttribute("contactError", true);
            return "redirect:/#contact";
        }
        contactMessageRepository.save(contactMessage);
        return "redirect:/?sent=true#contact";
    }

    static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
