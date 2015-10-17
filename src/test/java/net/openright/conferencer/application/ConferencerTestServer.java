package net.openright.conferencer.application;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.servlet.ServletHolder;

import net.openright.conferencer.application.profile.UserProfile;
import net.openright.infrastructure.server.EmbeddedWebAppContext;

public class ConferencerTestServer extends ConferencerServer {

    public class LoginSimulator extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            UserProfile userProfile = new UserProfile();
            userProfile.setEmail(req.getParameter("username"));
            req.getSession().setAttribute("userProfile", userProfile);
            resp.sendRedirect(req.getContextPath() + "/#profile");
        }

    }

    public ConferencerTestServer(ConferencerConfig config) {
        super(config);
    }

    @Override
    protected EmbeddedWebAppContext createWebAppContext() {
        EmbeddedWebAppContext appContext = super.createWebAppContext();
        appContext.addServlet(new ServletHolder(new LoginSimulator()), "/simulateLogin");
        return appContext;
    }

    public static void main(String[] args) throws Exception {
        ConferencerTestServer server = new ConferencerTestServer(new ConferencerConfigFile());
        server.start();
    }

}
