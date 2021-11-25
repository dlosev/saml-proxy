package com.ldv.samlproxy.controller.admin;

import com.ldv.samlproxy.service.ConfigManager;
import com.ldv.samlproxy.dto.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

import javax.validation.Valid;
import java.security.Principal;

/**
 * .
 *
 * @author Dmitry Losev ldv42@yandex.ru
 * @since 11/16/21
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ConfigManager configManager;

    @GetMapping("login")
    public String showLoginForm() {
        return "admin/login";
    }

    @GetMapping("config")
    public String showConfigForm(Model model) throws Exception {
        return "admin/config";
    }

    @PostMapping("config")
    private String saveConfig(@Valid @ModelAttribute("config") Config config, BindingResult result, Model model) throws Exception {
        if(result.hasErrors()) {
            return "admin/config";
        }

        configManager.saveConfig(config);

        eventPublisher.publishEvent(new RefreshEvent(this, "RefreshEvent", "Refreshing scope"));

        return "redirect:config";
    }

    @GetMapping({"", "/"})
    public RedirectView redirectWithUsingRedirectView(Principal principal) {
        return new RedirectView(principal == null ? "admin/login" : "admin/config");
    }

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("config", configManager.loadConfig());
    }
}
