package net.openright.infrastructure.rest;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

public interface JSONSource {

    @Nonnull
    JSONObject getJSON(HttpServletRequest req);

}
