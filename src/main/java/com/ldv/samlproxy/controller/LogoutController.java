package com.ldv.samlproxy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * .
 *
 * @author Dmitry Losev ldv42@yandex.ru
 * @since 11/23/21
 */
@Controller
@RequestMapping("/logout2")
public class LogoutController {

    @GetMapping
    public String showLogoutPage() {
        return "logout";
    }
}
