package net.openright.conferencer.application;

import java.io.IOException;
import java.nio.file.Path;

import javax.annotation.Nonnull;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.eclipse.jetty.plus.jndi.EnvEntry;

import net.openright.infrastructure.config.AppConfigFile;
import net.openright.infrastructure.db.Database;
import net.openright.infrastructure.util.ExceptionUtil;
import net.openright.infrastructure.util.IOUtil;

public class ConferencerConfigFile extends AppConfigFile implements ConferencerConfig {

    private Database database;

    public ConferencerConfigFile() throws IOException {
        super(IOUtil.extractResourceFile("conferencer.properties"));
    }

    public ConferencerConfigFile(Path configFile) {
        super(configFile);
    }

    @Override
    @Nonnull
    public String getGoogleClientId() {
        return getRequiredProperty("conferencer.google.client_id");
    }

    @Override
    @Nonnull
    public String getGoogleClientSecret() {
        return getRequiredProperty("conferencer.google.client_secret");
    }

    protected DataSource createDataSource() {
        if (System.getenv("DATABASE_URL") != null) {
            return migrateDataSource("conferencer", createDataSourceFromEnv(System.getenv("DATABASE_URL")));
        }
        return createDataSource("conferencer");
    }

    @Override
    public int getHttpPort() {
        if (System.getenv("PORT") != null) {
            return Integer.parseInt(System.getenv("PORT"));
        }
        return Integer.parseInt(getProperty("conferencer.http.port", "8000"));
    }

    @Override
    @Nonnull
    public synchronized Database getDatabase() {
        if (database == null) {
            this.database = new Database("jdbc/conferencerDs");
        }
        return database;
    }

    @Override
    public void start() {
        try {
            new EnvEntry("jdbc/conferencerDs", createDataSource());
            new EnvEntry("seedapp/config", this);
        } catch (NamingException e) {
            throw ExceptionUtil.soften(e);
        }
    }
}
