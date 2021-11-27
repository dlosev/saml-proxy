package com.ldv.samlproxy.controller.admin;

import com.ldv.samlproxy.dto.config.IdpConfig;
import com.ldv.samlproxy.dto.config.SpConfig;
import com.ldv.samlproxy.dto.config.SystemConfig;
import com.ldv.samlproxy.service.ConfigManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
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

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ConfigManager configManager;

    @Autowired
    private RelyingPartyRegistrationRepository relyingPartyRegistrationRepository;

    @Autowired
    private RelyingPartyRegistrationResolver relyingPartyRegistrationResolver;

    @GetMapping({"", "/"})
    public RedirectView redirect2Home(Principal principal) {
        return new RedirectView(principal == null ? "admin/login" : "admin/config");
    }

    @GetMapping("login")
    public String showLoginForm() {
        return "admin/login";
    }

    @GetMapping(value = "idp-metadata", produces = MediaType.APPLICATION_XML_VALUE)
    public @ResponseBody String getIdpMetadata() {
        return configManager.readMetadata();
    }

    @GetMapping({"config", "config/"})
    public String showOverviewPage(Model model, HttpServletRequest request) {
        RelyingPartyRegistration relyingPartyRegistration = relyingPartyRegistrationResolver.resolve(request, "idp");

        if (configManager.loadIdpConfig().getIdpMetadata() != null) {
            model.addAttribute("idpEntityId", relyingPartyRegistration.getAssertingPartyDetails().getEntityId());
        }
        model.addAttribute("spEntityId", relyingPartyRegistration.getEntityId());

        model.addAttribute("mode", "overview");

        return "admin/config";
    }

    @GetMapping( "config/sp")
    public String showSpConfigForm(Model model) {
        model.addAttribute("spConfig", configManager.loadSpConfig());
        model.addAttribute("mode", "sp");

        return "admin/config";
    }

    @GetMapping( "config/idp")
    public String showIdpConfigForm(Model model) {
        IdpConfig idpConfig = configManager.loadIdpConfig();

        if (idpConfig.getIdpMetadata() != null) {
            model.addAttribute("idpEntityId",
                    relyingPartyRegistrationRepository.findByRegistrationId("idp").getAssertingPartyDetails().getEntityId());
        }

        model.addAttribute("idpConfig", idpConfig);
        model.addAttribute("mode", "idp");

        return "admin/config";
    }

    @GetMapping( "config/system")
    public String showSystemConfigForm(Model model) {
        model.addAttribute("systemConfig", configManager.loadSystemConfig());
        model.addAttribute("mode", "system");

        return "admin/config";
    }

    @PostMapping( "config/system")
    private String saveSystemConfig(@Valid @ModelAttribute("systemConfig") SystemConfig config, BindingResult result,
                                 Model model, RedirectAttributes attrs) throws Exception {
        if (result.hasErrors()) {
            model.addAttribute("mode", "system");

            return "admin/config";
        }

        configManager.saveSystemConfig(config);
        reloadAppProperties();

        attrs.addFlashAttribute("success", true);

        return "redirect:/admin/config/system";
    }

    @PostMapping( "config/idp")
    private String saveIdpConfig(@Valid @ModelAttribute("idpConfig") IdpConfig config, BindingResult result,
                                 Model model, RedirectAttributes attrs) throws Exception {
        if (result.hasErrors()) {
            model.addAttribute("mode", "idp");

            return "admin/config";
        }

        configManager.saveIdpConfig(config);
        reloadAppProperties();

        attrs.addFlashAttribute("success", true);

        return "redirect:/admin/config/idp";
    }

    @PostMapping( "config/sp")
    private String saveSpConfig(@Valid @ModelAttribute("spConfig") SpConfig config, BindingResult result, Model model,
                                RedirectAttributes attrs) throws Exception {
        if (result.hasErrors()) {
            model.addAttribute("mode", "sp");

            return "admin/config";
        }

        configManager.saveSpConfig(config);
        reloadAppProperties();

        attrs.addFlashAttribute("success", true);

        return "redirect:/admin/config/sp";
    }

    private void reloadAppProperties() {
        eventPublisher.publishEvent(new RefreshEvent(this, "RefreshEvent", "Refreshing scope"));
    }
}
