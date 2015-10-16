package net.openright.infrastructure.rest;

import javax.servlet.http.HttpServletRequest;

public class RequestUrl {

    private HttpServletRequest req;

    public RequestUrl(HttpServletRequest req) {
        this.req = req;
    }

    @Override
    public String toString() {
        StringBuffer requestURL = req.getRequestURL();
        if (req.getQueryString() != null) {
            requestURL.append("?").append(req.getQueryString());
        }
        return requestURL.toString();
    }

}
