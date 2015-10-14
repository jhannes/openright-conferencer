package net.openright.conferencer.application;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Base64;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import net.openright.conferencer.application.profile.UserProfile;
import net.openright.infrastructure.rest.RequestException;
import net.openright.infrastructure.util.IOUtil;

public class AuthServlet extends HttpServlet {

    private ConferencerConfig config;

    @Override
    public void init() throws ServletException {
        this.config = (ConferencerConfig)getServletContext().getAttribute("config");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getPathInfo().equals("/provider")) {
            JSONObject json = new JSONObject()
                    .put("provider", getProvider(req));
            try(PrintWriter writer = resp.getWriter()) {
                resp.setContentType("application/json");
                json.write(writer);
            }
        } else if (req.getPathInfo().equals("/callback")) {
            String tokenUrl = "https://accounts.google.com/o/oauth2/token";
            String tokenRequest = String.format(
                    "code=%s&client_id=%s&client_secret=%s&redirect_uri=%s&grant_type=authorization_code",
                    req.getParameter("code"), config.getGoogleClientId(), config.getGoogleClientSecret(), getContextUrl(req) + "/auth/callback");
                    ;
            JSONObject tokenResponse = new JSONObject(IOUtil.postString(tokenRequest, tokenUrl));

            req.getSession().setAttribute("userProfile", toUserProfile(tokenResponse));
            resp.sendRedirect(req.getContextPath() + "/#profile");
        } else {
            throw new RequestException(404, "Not found");
        }
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

    private JSONObject getProvider(HttpServletRequest req) {
        String url = "https://accounts.google.com/o/oauth2/auth"
                + "?response_type=code"
                + "&client_id=" + config.getGoogleClientId()
                + "&scope=profile+email"
                + "&redirect_uri=" + getContextUrl(req) + "/auth/callback";
        return new JSONObject()
                .put("url", url);
    }

    public static String getContextUrl(HttpServletRequest req) {
        return req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort()
                + req.getContextPath();
    }
}
