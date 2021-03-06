package net.openright.conferencer.application;

import net.openright.infrastructure.server.EmbeddedWebAppContext;
import net.openright.infrastructure.server.ServerUtil;
import net.openright.infrastructure.server.StatusHandler;
import net.openright.infrastructure.util.IOUtil;
import net.openright.infrastructure.util.LogUtil;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.MovedContextHandler;
import org.eclipse.jetty.server.handler.ShutdownHandler;

import java.io.File;

public class ConferencerServer {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConferencerServer.class);
    private ConferencerConfig config;
    private Server server;

    public ConferencerServer(ConferencerConfig config) {
        this.config = config;
    }

    public static void main(String[] args) throws Exception {
        new File("logs").mkdirs();
        LogUtil.setupLogging("logging-conferencer.xml");
        IOUtil.extractResourceFile("conferencer.properties");

        ConferencerServer server = new ConferencerServer(new ConferencerConfigFile());
        server.start();

        if (System.getProperty("startBrowser") != null) {
            Runtime.getRuntime().exec("cmd /c \"start " + server.getURI() + "\"");
        }
    }

    protected void start() throws Exception {
        start(config.getHttpPort());
    }

    public void start(int port) throws Exception {
        server = new Server(port);
        server.setHandler(createHandlers());
        server.start();

        log.info("Started server " + server.getURI());
    }

    private Handler createHandlers() {
        HandlerList handlers = new HandlerList();
        handlers.addHandler(new ShutdownHandler("sgds", false, true));
        handlers.addHandler(createWebAppContext());
        handlers.addHandler(new StatusHandler());
        handlers.addHandler(new MovedContextHandler(null, "/", "/conferencer"));

        return ServerUtil.createStatisticsHandler(
                ServerUtil.createRequestLogHandler(handlers));
    }

    protected EmbeddedWebAppContext createWebAppContext() {
        EmbeddedWebAppContext webAppContext = new EmbeddedWebAppContext("/conferencer");
        webAppContext.getServletContext().setAttribute("config", config);
        return webAppContext;
    }

    public String getURI() {
        return "http://localhost:" + server.getURI().getPort();
    }

}
