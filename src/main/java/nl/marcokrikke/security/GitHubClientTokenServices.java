package nl.marcokrikke.security;

import nl.marcokrikke.model.User;
import nl.marcokrikke.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.MapBasedAttributes2GrantedAuthoritiesMapper;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class GitHubClientTokenServices extends RemoteTokenServices {
    private Logger log = LoggerFactory.getLogger(GitHubClientTokenServices.class);

    public static final String GITHUB_API_USER_URL = "https://api.github.com/user";
    public static final String GITHUB_API_TEAMS_URL = "https://api.github.com/user/teams";
    public static final String GITHUB_API_EMAILS_URL = "https://api.github.com/user/emails";

    public static final String GITHUB_SCOPES_HEADER = "X-OAuth-Scopes";
    public static final String GITHUB_CLIENTID_HEADER = "X-OAuth-Client-Id";

    private RestOperations restTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private MapBasedAttributes2GrantedAuthoritiesMapper roleMapper;

    @Value("${github.scope}")
    private String scope;

    @Value("${github.clientid}")
    private String clientId;


    public GitHubClientTokenServices() {
        restTemplate = new RestTemplate();

        ((RestTemplate) restTemplate).setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            // Ignore 400
            public void handleError(ClientHttpResponse response) throws IOException {
                if (response.getRawStatusCode() != 400) {
                    super.handleError(response);
                }
            }
        });
    }

    @Override
    public OAuth2Authentication loadAuthentication(String accessToken)
            throws AuthenticationException, InvalidTokenException {
        log.debug("Loading OAuth2Authentication for access token {}", accessToken);

        // Get the GitHub user details from the GitHub API
        GitHubUser gitHubUser = getGitHubUser(accessToken);

        if (!clientId.equals(gitHubUser.getClientId())) {
            throw new InvalidTokenException("Invalid client id: " + gitHubUser.getClientId());
        }

        // Check if the authenticated user already exists in the database
        User user = userService.find(gitHubUser.getLogin());

        if (user == null) {
            // Provision new user in database
            User newUser = new User.Builder().name(gitHubUser.getName())
                    .email(getPrimaryGitHubEmailAddress(accessToken).getEmail()).gitHubUsername(gitHubUser.getLogin())
                    .username(gitHubUser.getLogin()).avatarUrl(gitHubUser.getAvatarUrl()).build();

            log.debug("GitHub user {} does not exist in database; provisioning new user .", gitHubUser.getLogin(),
                    newUser);

            user = userService.create(newUser);
        } else {
            log.debug("GitHub user {} already exist in database", gitHubUser.getLogin());
        }

        // Get all granted authorities
        List<GrantedAuthority> grantedAuthorities = getGrantedAuthorities(accessToken);

        // Create the userdetails
        GitHubUserDetails userDetails = new GitHubUserDetails(user.getUsername(), "N/A", grantedAuthorities);
        userDetails.setId(user.getId());
        userDetails.setUsername(user.getUsername());
        userDetails.setName(user.getName());
        userDetails.setEmail(user.getEmail());
        userDetails.setGitHubUsername(user.getGitHubUsername());
        userDetails.setAvatarUrl(user.getAvatarUrl());

        // Create a OAuth2Authentication
        OAuth2Authentication authentication =
                createOAuth2Authentication(userDetails, grantedAuthorities, gitHubUser.getScope(),
                        gitHubUser.getClientId());

        log.debug("Created OAuth2Authentication {}", authentication);

        return authentication;
    }


    private GitHubUser getGitHubUser(String accessToken) {
        ResponseEntity<GitHubUser> response = restTemplate
                .exchange(GITHUB_API_USER_URL, HttpMethod.GET, getGitHubRequestHeaders(accessToken), GitHubUser.class);

        GitHubUser gitHubUser = response.getBody();

        log.debug("Fetched {} from GitHub API", gitHubUser);

        // Get the assigned scope from the HTTP header
        if (response.getHeaders().containsKey(GITHUB_SCOPES_HEADER)) {
            Pattern p = Pattern.compile(",");

            Set<String> scopes =
                    p.splitAsStream(response.getHeaders().get(GITHUB_SCOPES_HEADER).get(0)).map(String::trim)
                            .collect(Collectors.toSet());

            log.debug("Assigning scope {} to user", scopes);

            gitHubUser.setScope(scopes);
        }

        // Get the assigned clientId from the HTTP header
        if (response.getHeaders().containsKey(GITHUB_CLIENTID_HEADER)) {
            gitHubUser.setClientId(response.getHeaders().get(GITHUB_CLIENTID_HEADER).get(0));

            log.debug("Assigning clientId {} to user", gitHubUser.getClientId());
        }

        return gitHubUser;
    }

    private List<GitHubTeam> getGitHubTeams(String accessToken) {
        return asList(restTemplate.exchange(GITHUB_API_TEAMS_URL, HttpMethod.GET, getGitHubRequestHeaders(accessToken),
                GitHubTeam[].class).getBody());
    }

    private List<GrantedAuthority> getGrantedAuthorities(String accessToken) {
        List<GitHubTeam> gitHubTeams = getGitHubTeams(accessToken);

        log.debug("Fetched teams {} from GitHub API", gitHubTeams);

        List<String> gitHubTeamIds = gitHubTeams.stream().map(GitHubTeam::getId).collect(Collectors.toList());

        List<GrantedAuthority> grantedAuthorities = roleMapper.getGrantedAuthorities(gitHubTeamIds);

        log.debug("Mapped GitHub teams to granted authorities {}", grantedAuthorities);

        return grantedAuthorities;
    }

    private List<GitHubEmail> getGitHubEmailAddresses(String accessToken) {
        return asList(restTemplate.exchange(GITHUB_API_EMAILS_URL, HttpMethod.GET, getGitHubRequestHeaders(accessToken),
                GitHubEmail[].class).getBody());
    }

    private GitHubEmail getPrimaryGitHubEmailAddress(String accessToken) {
        Optional<GitHubEmail> first =
                getGitHubEmailAddresses(accessToken).stream().filter(e -> e.isPrimary()).findFirst();

        GitHubEmail email = first.isPresent() ? first.get() : null;

        log.debug("Found primary email address {}", email);

        return email;
    }

    private OAuth2Authentication createOAuth2Authentication(GitHubUserDetails userDetails,
                                                            List<GrantedAuthority> authorities, Set<String> scope,
                                                            String clientId) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("client_id", clientId);

        Authentication user = new UsernamePasswordAuthenticationToken(userDetails, "N/A", authorities);

        OAuth2Request request =
                new OAuth2Request(parameters, clientId, authorities, true, scope, Collections.<String>emptySet(), null,
                        null, null);

        return new OAuth2Authentication(request, user);
    }

    private HttpEntity<Object> getGitHubRequestHeaders(String accessToken) {
        // We are not fully authenticated yet, so we cannot use the OAuth2RestTemplate
        // Add an Authorization header using the accessToken as username and no password.

        String authHeader;
        String credentials = String.format("%s:", accessToken);

        try {
            authHeader = "Basic " + new String(Base64.encode(credentials.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Could not convert String");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);

        return new HttpEntity<>(null, headers);
    }
}
