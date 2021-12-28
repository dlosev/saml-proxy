package com.ldv.samlproxy.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * .
 *
 * @author Dmitry Losev ldv42@yandex.ru
 * @since 11/25/21
 */
public class SamlConfigurationFilter extends OncePerRequestFilter {

    private final RequestMatcher requestMatcher;

    @Value("${custom.idp-metadata-location}")
    private Resource idpMetadata;

    public SamlConfigurationFilter(String registrationId) {
        requestMatcher = new AntPathRequestMatcher(String.format("/saml2/authenticate/%s", registrationId));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!this.requestMatcher.matcher(request).isMatch() || idpMetadata.exists()) {
            filterChain.doFilter(request, response);
        } else {
            throw new ServletException("IDP metadata isn't configured");
        }
    }
}
