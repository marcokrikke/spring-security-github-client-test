package nl.marcokrikke.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;

@Configuration
@EnableOAuth2Client
@ImportResource({ "classpath:security-context.xml" })
class OAuth2SecurityConfiguration {

}
