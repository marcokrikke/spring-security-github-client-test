package nl.marcokrikke.controller;

import nl.marcokrikke.model.GitHubOrganisation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static java.util.Arrays.asList;

@Controller
public class IndexController {
    private Logger log = LoggerFactory.getLogger(IndexController.class);

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public String index(Authentication authentication) {

        // We should be authenticated here. Grab all organisations of the logged in user using the OAuth2RestTemplate
        List<GitHubOrganisation> orgs =
                asList(restTemplate.getForObject("https://api.github.com/user/orgs", GitHubOrganisation[].class));

        log.debug("Found organisations {}", orgs);

        return authentication.getPrincipal().toString();
    }
}
