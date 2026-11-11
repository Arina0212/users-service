package ru.urfu.users.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secret;
    private Duration ttl = Duration.ofHours(1);
    private String issuer = "users-service";

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Duration getTtl() {
        return ttl == null ? Duration.ofHours(1) : ttl;
    }

    public void setTtl(Duration ttl) {
        this.ttl = ttl;
    }

    public String getIssuer() {
        return issuer == null || issuer.isBlank() ? "users-service" : issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}
