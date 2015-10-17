package net.openright.conferencer.application.profile;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import net.openright.infrastructure.rest.RequestException;

public class UserProfile {

    private final static ThreadLocal<UserProfile> current = new ThreadLocal<>();
    private String email;
    private String accessToken;
    private String identityProvider;

    @Nonnull
    public static UserProfile getCurrent() {
        if (current.get() == null) {
            throw new RequestException(401, "Unauthorized");
        }
        return current.get();
    }

    @Nonnull
    @CheckReturnValue
    public AutoCloseable setAsCurrent() {
        if (current.get() != null) {
            throw new IllegalStateException("Duplicate setting of UserProfile.current");
        }
        current.set(this);
        return () -> current.remove();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getIdentityProvider() {
        return identityProvider;
    }

    public void setIdentityProvider(String identityProvider) {
        this.identityProvider = identityProvider;
    }


}
