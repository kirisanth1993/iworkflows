package com.kajan.iworkflows.controller;

import com.kajan.iworkflows.model.Oauth2Token;
import com.kajan.iworkflows.service.OauthControllerService;
import com.kajan.iworkflows.util.Constants.OauthRegistrationId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
public class OauthController {

    private Logger logger = LoggerFactory.getLogger(OauthController.class);

    @Autowired
    private OauthControllerService oauthControllerService;

    private final String authorizationRequestBaseUri = "authorize/oauth2";
    Map<String, String> oauth2AuthenticationUrls = new HashMap<>();
    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @GetMapping("/authorize")
    public String getAuthorizationPage(Model model) {
        Iterable<ClientRegistration> clientRegistrations = null;
        ResolvableType type = ResolvableType.forInstance(clientRegistrationRepository)
                .as(Iterable.class);
        if (type != ResolvableType.NONE && ClientRegistration.class.isAssignableFrom(type.resolveGenerics()[0])) {
            clientRegistrations = (Iterable<ClientRegistration>) clientRegistrationRepository;
        }

        clientRegistrations.forEach(registration -> oauth2AuthenticationUrls.put(registration.getClientName(), authorizationRequestBaseUri + "/" + registration.getRegistrationId()));
        model.addAttribute("urls", oauth2AuthenticationUrls);

        return "authorize";
    }

    @RequestMapping("/authorize/oauth2/{registrationId}")
    public ModelAndView redirectToNextCloudForAuthorization(@PathVariable String registrationId) {

        logger.debug("hit /authorize/oauth2/" + registrationId + " end-point");

        OauthRegistrationId oauthRegistrationId = OauthRegistrationId.valueOf(registrationId.toUpperCase());

        URI requestURI = oauthControllerService.getAuthorizationCodeRequestUri(oauthRegistrationId);
        return new ModelAndView("redirect:" + requestURI.toASCIIString());
    }

    @RequestMapping("/login/oauth2/code/{registrationId}")
    public String getNextcloudAccessToken(@PathVariable String registrationId, HttpServletRequest httpServletRequest, Principal principal, Model model) {

        logger.debug("hit /login/oauth2/code/" + registrationId + " end-point");

        OauthRegistrationId oauthRegistrationId = OauthRegistrationId.valueOf(registrationId.toUpperCase());
        oauthControllerService.exchangeAuthorizationCodeForAccessToken(oauthRegistrationId, httpServletRequest, principal);
        Oauth2Token oauth2Token = oauthControllerService.getOauth2Tokens(principal, oauthRegistrationId);
        model.addAttribute("accessToken", oauth2Token.getAccessToken());
        model.addAttribute("refreshToken", oauth2Token.getRefreshToken());
        return "authorization-success";
    }
}
