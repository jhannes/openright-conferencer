package net.openright.conferencer.application;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.openright.conferencer.application.profile.ProfileApiController;
import net.openright.conferencer.application.profile.UserProfile;
import net.openright.conferencer.domain.comments.CommentApiController;
import net.openright.conferencer.domain.events.EventApiController;
import net.openright.conferencer.domain.talks.TalkApiController;
import net.openright.infrastructure.rest.ApiFrontController;
import net.openright.infrastructure.rest.Controller;
import net.openright.infrastructure.rest.GetJSONController;
import net.openright.infrastructure.rest.JsonResourceController;
import net.openright.infrastructure.rest.RequestException;

public class ConferencerSecureFrontServlet extends ApiFrontController {

    private static final long serialVersionUID = 5677329709881148525L;

    private static Logger log = LoggerFactory.getLogger(ConferencerSecureFrontServlet.class);

    private ConferencerConfig config;

    @Override
    public void init() throws ServletException {
        this.config = (ConferencerConfig)getServletContext().getAttribute("config");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (AutoCloseable context = getCurrentUser(req).setAsCurrent()) {
            super.service(req, resp);
        } catch (RequestException e) {
            log.info("Request failed {}: {}", req.getRequestURL(), e.toString());
            resp.sendError(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Request failed {}", req.getRequestURL(), e);
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
            case "profile": return new GetJSONController(new ProfileApiController(config));
            case "events": return new JsonResourceController(new EventApiController(config));
            case "talks": return new JsonResourceController(new TalkApiController(config));
            case "comments": return new JsonResourceController(new CommentApiController(config));
            default: return null;
        }
    }

}
