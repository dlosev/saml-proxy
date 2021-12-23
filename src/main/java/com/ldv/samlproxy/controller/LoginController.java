package com.ldv.samlproxy.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

/**
 * .
 *
 * @author Dmitry Losev ldv42@yandex.ru
 * @since 11/14/21
 */
@Controller
@RefreshScope
@RequestMapping("/auth")
public class LoginController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

    @Value("${custom.login-redirect-url}")
    private String loginRedirectUrl;

    @GetMapping
    public RedirectView redirectWithUsingRedirectView(@AuthenticationPrincipal Saml2AuthenticatedPrincipal principal) {
        LOGGER.debug("Redirecting to {} since request is authenticated for user {}",
                loginRedirectUrl, principal.getName());

        return new RedirectView(loginRedirectUrl);
    }
}
