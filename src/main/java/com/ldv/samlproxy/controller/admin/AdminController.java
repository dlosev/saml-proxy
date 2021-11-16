package com.ldv.samlproxy.controller.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

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

    @GetMapping("login")
    public String showLoginForm() {
        return "/admin/login";
    }

    @GetMapping("config")
    public String showConfigForm() {
        return "/admin/config";
    }

    @GetMapping({"", "/"})
    public RedirectView redirectWithUsingRedirectView(Principal principal) {
        return new RedirectView(principal == null ? "/admin/login" : "/admin/config");
    }
}
