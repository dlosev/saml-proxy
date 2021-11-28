package com.ldv.samlproxy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

/**
 * .
 *
 * @author Dmitry Losev ldv42@yandex.ru
 * @since 11/16/21
 */
@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @RequestMapping("/error")
    public String exception(Model model, HttpServletRequest request) {
        if (!model.containsAttribute(SimpleMappingExceptionResolver.DEFAULT_EXCEPTION_ATTRIBUTE) &&
                request.getAttribute(RequestDispatcher.ERROR_EXCEPTION) != null) {
            model.addAttribute(SimpleMappingExceptionResolver.DEFAULT_EXCEPTION_ATTRIBUTE,
                    request.getAttribute(RequestDispatcher.ERROR_EXCEPTION));
        }

        return "error";
    }
}
