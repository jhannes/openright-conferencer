package net.openright.conferencer.application;

import net.openright.conferencer.application.profile.ProfileApiController;
import net.openright.conferencer.domain.orders.OrdersApiController;
import net.openright.conferencer.domain.products.ProductsApiController;
import net.openright.infrastructure.rest.ApiFrontController;
import net.openright.infrastructure.rest.Controller;
import net.openright.infrastructure.rest.JsonResourceController;
import javax.servlet.ServletException;

public class ConferencerFrontServlet extends ApiFrontController {

    private ConferencerConfig config;

    @Override
    public void init() throws ServletException {
        this.config = (ConferencerConfig)getServletContext().getAttribute("config");
    }

    @Override
    protected Controller getControllerForPath(String prefix) {
        switch (prefix) {
            case "orders": return new JsonResourceController(new OrdersApiController(config));
            case "products": return new JsonResourceController(new ProductsApiController(config));
            case "profile": return new JsonResourceController(new ProfileApiController(config));
            default: return null;
        }
    }
}
