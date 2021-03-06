package net.openright.infrastructure.rest;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.Optional;

public class JsonResourceController implements Controller {
    private static Logger log = LoggerFactory.getLogger(JsonResourceController.class);

    private final ResourceApi resourceApi;

    public JsonResourceController(ResourceApi applicationApi) {
        this.resourceApi = applicationApi;
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        try {
            doHandle(req, resp);
        } catch (JSONException e) {
            log.warn("{}", req.getRequestURL(), e);
            throw new RequestException(400, e.getMessage());
        }
    }

    private void doHandle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (req.getMethod().equals("POST")) {
            createResource(req, resp);
        } else if (req.getMethod().equals("PUT")) {
            updateResource(req, resp);
        } else if (req.getMethod().equals("GET")) {
            getResource(req, resp);
        } else {
            resp.sendError(400);
        }
    }

    private void getResource(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = getResourceId(req);
        if (id != null) {
            sendResponse(resp, resourceApi.getResource(id));
        } else {
            sendResponse(resp, Optional.of(resourceApi.listResources()));
        }
    }

    protected String getResourceId(HttpServletRequest req) {
        String[] parts = req.getPathInfo().split("/");
        return parts.length > 2 ? parts[2] : null;
    }

    private void updateResource(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (BufferedReader reader = req.getReader()) {
            JSONObject jsonObject = new JSONObject(new JSONTokener(reader));
            resourceApi.updateResource(getResourceId(req), jsonObject);
            resp.setStatus(HttpServletResponse.SC_OK);
        }
    }

    private void sendResponse(HttpServletResponse resp, Optional<JSONObject> optional) throws IOException {
        resp.setHeader("Expires", "-1");
        if (!optional.isPresent()) {
            resp.setStatus(204);
            return;
        }
        resp.setContentType("application/json");
        try (Writer writer = resp.getWriter()) {
            writer.write(optional.get().toString());
        }
    }

    private void createResource(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JSONObject jsonObject;
        try (BufferedReader reader = req.getReader()) {
            jsonObject = new JSONObject(new JSONTokener(reader));
        }
        if (getResourceId(req) != null) {
            resourceApi.updateResource(getResourceId(req), jsonObject);
        } else {
            String id = String.valueOf(resourceApi.createResource(jsonObject));
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.setHeader("Location", req.getRequestURL() + id);
        }
    }
}
