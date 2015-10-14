package net.openright.conferencer.application;

import java.io.IOException;
import java.util.Base64;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import net.openright.conferencer.application.profile.UserProfile;
import net.openright.infrastructure.rest.ApiFrontController;
import net.openright.infrastructure.rest.Controller;
import net.openright.infrastructure.rest.GetJSONController;
import net.openright.infrastructure.rest.JSONSource;
import net.openright.infrastructure.util.IOUtil;

public class AuthServlet extends ApiFrontController {

    public class ProfileController implements JSONSource {
        @Override
        @Nonnull
        public JSONObject getJSON(HttpServletRequest req) {
            return new JSONObject()
                    .put("provider", new JSONObject().put("url", getURL(req)));
        }

        private String getURL(HttpServletRequest req) {
            return String.format("https://accounts.google.com/o/oauth2/auth"
                    + "?response_type=code"
                    + "&client_id=%s"
                    + "&scope=profile+email"
                    + "&redirect_uri=%s",
                    config.getGoogleClientId(), getContextUrl(req) + "/auth/callback");
        }
    }

    public class CallbackController implements Controller {

        @Override
        public void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
            String tokenUrl = "https://accounts.google.com/o/oauth2/token";

            String tokenRequest = String.format(
                    "code=%s&client_id=%s&client_secret=%s&redirect_uri=%s&grant_type=authorization_code",
                    req.getParameter("code"), config.getGoogleClientId(), config.getGoogleClientSecret(), getContextUrl(req) + "/auth/callback");
            JSONObject tokenResponse = new JSONObject(IOUtil.postString(tokenRequest, tokenUrl));

            req.getSession().setAttribute("userProfile", toUserProfile(tokenResponse));
            resp.sendRedirect(req.getContextPath() + "/#profile");
        }

        private UserProfile toUserProfile(JSONObject tokenResponse) {
            UserProfile userProfile = new UserProfile();
            userProfile.setIdentityProvider("google");
            userProfile.setAccessToken(tokenResponse.getString("access_token"));
            JSONObject idToken = parseIdToken(tokenResponse.getString("id_token"));
            userProfile.setEmail(idToken.getString("email"));
            return userProfile;
        }

        private JSONObject parseIdToken(String idToken) {
            return new JSONObject(new String(Base64.getDecoder().decode(idToken.split("\\.")[1])));
        }

    }

    private ConferencerConfig config;

    @Override
    public void init() throws ServletException {
        this.config = (ConferencerConfig)getServletContext().getAttribute("config");
    }

    @Override
    protected Controller getControllerForPath(String prefix) {
        switch (prefix) {
        case "provider": return new GetJSONController(new ProfileController());
        case "callback": return new CallbackController();
        }
        return null;
    }

    public static String getContextUrl(HttpServletRequest req) {
        return req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort()
                + req.getContextPath();
    }
}
