package com.ldv.samlproxy.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

/**
 * .
 *
 * @author Dmitry Losev ldv42@yandex.ru
 * @since 12/28/21
 */
public class SamlSessionExpirationFilter extends OncePerRequestFilter {

    public static final String SESSION_SAML_NOT_ON_OR_AFTER_ATTRIBUTE = "sessionSamlNotOnOrAfterAttribute";
    public static final Duration DEFAULT_CLOCK_SKEW = Duration.ofMinutes(1);

    public static final Logger LOGGER = LoggerFactory.getLogger(SamlSessionExpirationFilter.class);

    private final RequestMatcher requestMatcher;

    @Autowired
    private Environment env;

    public SamlSessionExpirationFilter(String authRequestPattern) {
        requestMatcher = new AntPathRequestMatcher(authRequestPattern);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (this.requestMatcher.matcher(request).isMatch() &&
                env.getRequiredProperty("custom.saml-session-expiration", Boolean.TYPE)) {
            HttpSession session = request.getSession();

            Instant notOnOrAfter = (Instant) session.getAttribute(SESSION_SAML_NOT_ON_OR_AFTER_ATTRIBUTE);

            if (notOnOrAfter != null && !Instant.now().minus(DEFAULT_CLOCK_SKEW).isBefore(notOnOrAfter)) {
                LOGGER.debug("SAML session is expired according to [NotOnOrAfter] SAML attribute {}. Invalidating HTTP session", notOnOrAfter);

                SecurityContextHolder.getContext().setAuthentication(null);

                session.invalidate();
            }
        }

        chain.doFilter(request, response);
    }
}
