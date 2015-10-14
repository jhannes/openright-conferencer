package net.openright.conferencer.application;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.openright.conferencer.application.profile.ProfileApiController;
import net.openright.conferencer.application.profile.UserProfile;
import net.openright.infrastructure.rest.ApiFrontController;
import net.openright.infrastructure.rest.Controller;
import net.openright.infrastructure.rest.GetJSONController;
import net.openright.infrastructure.rest.RequestException;

public class ConferencerSecureFrontServlet extends ApiFrontController {

    @SuppressWarnings("unused")
    private ConferencerConfig config;

    @Override
    public void init() throws ServletException {
        this.config = (ConferencerConfig)getServletContext().getAttribute("config");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (AutoCloseable context = UserProfile.setCurrent(getCurrentUser(req))) {
            super.service(req, resp);
        } catch (RequestException e) {
            resp.sendError(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            resp.sendError(500, e.toString());
        }
    }

    @Nonnull
    private UserProfile getCurrentUser(HttpServletRequest req) {
        UserProfile userProfile = (UserProfile) req.getSession().getAttribute("userProfile");
        if (userProfile == null) {
            throw new RequestException(401, "Not logged in");
        }
        return userProfile;
    }

    @Override
    protected Controller getControllerForPath(String prefix) {
        switch (prefix) {
            case "profile": return new GetJSONController(new ProfileApiController());
            default: return null;
        }
    }

}
