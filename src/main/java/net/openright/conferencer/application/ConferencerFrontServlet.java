package net.openright.conferencer.application;

import net.openright.conferencer.domain.orders.OrdersApiController;
import net.openright.conferencer.domain.products.ProductsApiController;
import net.openright.infrastructure.rest.ApiFrontController;
import net.openright.infrastructure.rest.Controller;
import net.openright.infrastructure.rest.JsonResourceController;
import net.openright.infrastructure.util.ExceptionUtil;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;

public class ConferencerFrontServlet extends ApiFrontController {

    private ConferencerConfig config;

    @Override
    public void init() throws ServletException {
        try {
            this.config = (ConferencerConfig)new InitialContext().lookup("seedapp/config");
        } catch (NamingException e) {
            throw ExceptionUtil.soften(e);
        }
    }

    @Override
    protected Controller getControllerForPath(String prefix) {
        switch (prefix) {
            case "orders": return new JsonResourceController(new OrdersApiController(config));
            case "products": return new JsonResourceController(new ProductsApiController(config));
            default: return null;
        }
    }
}
