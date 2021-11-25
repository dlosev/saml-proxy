package com.ldv.samlproxy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * .
 *
 * @author Dmitry Losev ldv42@yandex.ru
 * @since 11/16/21
 */
@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @RequestMapping("/error")
    public String exception(Model model, HttpServletRequest request, HttpServletResponse response) {
        if (!model.containsAttribute(SimpleMappingExceptionResolver.DEFAULT_EXCEPTION_ATTRIBUTE)) {
            FlashMap flashMap = new SessionFlashMapManager().retrieveAndUpdate(request, response);

            if (flashMap != null && flashMap.get(SimpleMappingExceptionResolver.DEFAULT_EXCEPTION_ATTRIBUTE) != null) {
                model.addAttribute(SimpleMappingExceptionResolver.DEFAULT_EXCEPTION_ATTRIBUTE,
                        request.getAttribute(SimpleMappingExceptionResolver.DEFAULT_EXCEPTION_ATTRIBUTE));
            }
        }

        return "error";
    }
}
