package com.ldv.samlproxy.event.listener;

import com.ldv.samlproxy.filter.SamlSessionExpirationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * .
 *
 * @author Dmitry Losev ldv42@yandex.ru
 * @since 12/28/21
 */
@Component
public class AuthenticationSuccessEventListener {

    public static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationSuccessEventListener.class);

    @Autowired
    private HttpSession httpSession;

    @Autowired
    private Environment env;

    @EventListener
    public void onEvent(AuthenticationSuccessEvent event) {
        if (event.getAuthentication() instanceof Saml2Authentication &&
                env.getRequiredProperty("custom.saml-session-expiration", Boolean.TYPE)) {
            Instant notOnOrAfter = null;

            Matcher m = Pattern.compile("Conditions .*NotOnOrAfter=\"([^\"]*)\"")
                    .matcher(((Saml2Authentication) event.getAuthentication()).getSaml2Response());
            if (m.find()) {
                notOnOrAfter = Instant.parse(m.group(1));
            }

            if (notOnOrAfter != null) {
                LOGGER.debug("Saving [NotOnOrAfter] SAML attribute {} to the current HTTP session", notOnOrAfter);

                httpSession.setAttribute(SamlSessionExpirationFilter.SESSION_SAML_NOT_ON_OR_AFTER_ATTRIBUTE, notOnOrAfter);
            }
        }
    }
}
