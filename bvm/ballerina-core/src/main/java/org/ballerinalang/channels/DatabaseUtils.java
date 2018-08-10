package org.ballerinalang.channels;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

/**
 * Utility methods for storing/fetching channel messages.
 */
public class DatabaseUtils {

    private static HikariDataSource hikariDataSource;
    private static Connection con;
    private static HikariConfig config;
    private static String dbName = ChannelConstants.DB_NAME;

    private static void createDBConnection() {
        if (hikariDataSource == null) {
            config = new HikariConfig();
            config.setUsername(ChannelConstants.DB_USERNAME);
            config.setPassword(ChannelConstants.DB_PASSWORD);

            readConfigs();
            String jdbcUrl = "jdbc:h2:mem:";
            jdbcUrl.concat(dbName);
            config.setJdbcUrl(jdbcUrl);
            hikariDataSource = new HikariDataSource(config);

            try {
                con = hikariDataSource.getConnection();
                con.prepareStatement("create table messages (" +
                        "  msgId int NOT NULL AUTO_INCREMENT," +
                        "  channelName  varchar(200)," +
                        "  key    varchar(200)," +
                        "  value    varchar(200)," +
                        "  constraint pk primary key ( msgId )" +
                        ")").execute();
            } catch (SQLException e) {
                throw new BallerinaException("error in get connection to persist channel message " + e.getMessage(),
                        e);
            } finally {
                if (con != null) {
                    try {
                        con.close();
                    } catch (SQLException e) {
                        //ignore
                    }
                }
            }
        }
    }

    public static void addEntry(String channelName, BValue key, BValue value) {
        createDBConnection();
        String encodedKey = Base64.getEncoder().encodeToString(key.stringValue().getBytes());
        String encodedValue = Base64.getEncoder().encodeToString(value.stringValue().getBytes());
        String addStatement = "INSERT into messages (channelName, key, value) values ('"
                + channelName + "', '"
                + encodedKey + "', '"
                + encodedValue + "')";
        try {
            con = hikariDataSource.getConnection();
            con.prepareStatement(addStatement).execute();
        } catch (SQLException e) {
            throw new BallerinaException("error in get connection to persist channel message " + e.getMessage(),
                    e);
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                //ignore
            }
        }
    }

    public static BValue getMessage(String channelName, BValue key) {
        createDBConnection();
        String encodedKey = Base64.getEncoder().encodeToString(key.stringValue().getBytes());
        String stmt = "SELECT msgId,value FROM messages WHERE channelName = '" + channelName + "' AND key = '" +
                encodedKey + "'";
        ResultSet result;
        try {
            con = hikariDataSource.getConnection();
            result = con.prepareStatement(stmt).executeQuery();
            if (result.next()) {
                byte[] bytes = Base64.getDecoder().decode(result.getString(2));
                String stringVal = new String(bytes);
                int msgId = result.getInt(1);
                con.prepareStatement("DELETE FROM messages where msgId = " + msgId).execute();
                //todo: should fix for all types
                return new BString(stringVal);
            }
        } catch (SQLException e) {
            throw new BallerinaException("error in get connection to persist channel message " + e.getMessage(),
                    e);
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                //ignore
            }
        }
        return null;
    }

    private static void readConfigs() {
        ConfigRegistry registry = ConfigRegistry.getInstance();
        if (registry.contains(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_PASSWORD)) {
            config.setPassword(registry.getAsString(ChannelConstants.CONF_NAMESPACE +
                    ChannelConstants.CONF_PASSWORD));
        }

        if (registry.contains(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_USERNAME)) {
            config.setUsername(registry.getAsString(ChannelConstants.CONF_NAMESPACE +
                    ChannelConstants.CONF_USERNAME));
        }

        if (registry.contains(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_DB)) {
            dbName = registry.getAsString(ChannelConstants.CONF_NAMESPACE + ChannelConstants.CONF_DB);
        }

    }

}
