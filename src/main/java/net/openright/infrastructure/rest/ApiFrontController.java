package net.openright.infrastructure.rest;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class ApiFrontController extends HttpServlet {
    private static Logger log = LoggerFactory.getLogger(ApiFrontController.class);


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        try {
            log.debug("BEGIN {} {}", req.getMethod(), new RequestUrl(req));

            getController(req).handle(req, resp);
        } catch (RequestException e) {
            resp.sendError(e.getStatusCode(), e.getMessage());
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("COMPLETE {} {} in {}ms for {}",
                    req.getMethod(), new RequestUrl(req), duration, req.getRemoteHost());
        }
    }

    @Nonnull
    private Controller getController(HttpServletRequest req) {
        Controller defaultController = (request, res) -> super.service(request, res);
        Controller controller = getControllerForPath(req.getPathInfo().split("/")[1]);
        return controller != null ? controller : defaultController;
    }

    protected abstract Controller getControllerForPath(String prefix);
}
