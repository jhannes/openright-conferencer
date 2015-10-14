package net.openright.infrastructure.rest;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

public class GetJSONController implements Controller {

    private final JSONSource jsonSource;

    public GetJSONController(JSONSource jsonSource) {
        this.jsonSource = jsonSource;
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        JSONObject json = jsonSource.getJSON(req);
        try(PrintWriter writer = resp.getWriter()) {
            resp.setContentType("application/json");
            json.write(writer);
        }
    }

}