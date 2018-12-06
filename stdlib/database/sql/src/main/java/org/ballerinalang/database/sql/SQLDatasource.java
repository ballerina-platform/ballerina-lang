/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.database.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.connector.api.Value;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.sql.XADataSource;

/**
 * Native SQL Connector.
 *
 * @since 0.8.0
 */
public class SQLDatasource implements BValue {

    private HikariDataSource hikariDataSource;
    private String peerAddress;
    private String databaseName;
    private String databaseProductName;
    private String connectorId;
    private boolean xaConn;

    public SQLDatasource() {
    }

    public boolean init(SQLDatasourceParams sqlDatasourceParams) {
        databaseName = sqlDatasourceParams.dbName;
        peerAddress = sqlDatasourceParams.jdbcUrl;
        buildDataSource(sqlDatasourceParams);
        connectorId = UUID.randomUUID().toString();
        xaConn = isXADataSource();
        try (Connection con = getSQLConnection()) {
            databaseProductName = con.getMetaData().getDatabaseProductName().toLowerCase(Locale.ENGLISH);
        } catch (SQLException e) {
            throw new BallerinaException("error in get connection: " + Constants.CONNECTOR_NAME + ": " + e.getMessage(),
                    e);
        }
        return true;
    }

    /**
     * Get the peer address of this datasource. If URL is used, the peer address is the URL. Otherwise, the peer address
     * is "host:port"
     *
     * @return The peer address for this datasource.
     */
    public String getPeerAddress() {
        return peerAddress;
    }

    /**
     * Get the database name.
     *
     * @return The database name, or null if the URL is used.
     */
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * Get the database product name.
     *
     * @return The database product name.
     */
    public String getDatabaseProductName() {
        return databaseProductName;
    }

    public Connection getSQLConnection() {
        try {
            return hikariDataSource.getConnection();
        } catch (SQLException e) {
            throw new BallerinaException("error in get connection: " + Constants.CONNECTOR_NAME + ": " + e.getMessage(),
                    e);
        }
    }

    public String getConnectorId() {
        return this.connectorId;
    }

    public boolean isXAConnection() {
        return this.xaConn;
    }

    public XADataSource getXADataSource() {
        XADataSource xaDataSource;
        try {
            xaDataSource = hikariDataSource.unwrap(XADataSource.class);
        } catch (SQLException e) {
            throw new BallerinaException("error in get distributed data source");
        }
        return xaDataSource;
    }

    public void closeConnectionPool() {
        hikariDataSource.close();
    }

    private void buildDataSource(SQLDatasourceParams sqlDatasourceParams) {
        try {
            HikariConfig config = new HikariConfig();
            //Set username password
            config.setUsername(sqlDatasourceParams.username);
            config.setPassword(sqlDatasourceParams.password);
            //Set URL
            if (sqlDatasourceParams.jdbcUrl.isEmpty()) {
                sqlDatasourceParams.jdbcUrl = constructJDBCURL(sqlDatasourceParams.dbType,
                        sqlDatasourceParams.hostOrPath, sqlDatasourceParams.port, sqlDatasourceParams.dbName,
                        sqlDatasourceParams.username, sqlDatasourceParams.password, sqlDatasourceParams.urlOptions);
            }
            //Set optional properties
            if (sqlDatasourceParams.options != null) {
                boolean isXA = sqlDatasourceParams.options.getBooleanField(Constants.Options.IS_XA);
                BMap<String, BRefType<?>> dataSourceConfigMap = populatePropertiesMap(sqlDatasourceParams.dbOptionsMap);

                String dataSourceClassName = sqlDatasourceParams.options
                        .getStringField(Constants.Options.DATASOURCE_CLASSNAME);
                if (isXA && dataSourceClassName.isEmpty()) {
                    dataSourceClassName = getXADatasourceClassName(sqlDatasourceParams.dbType,
                            sqlDatasourceParams.jdbcUrl, sqlDatasourceParams.username, sqlDatasourceParams.password);
                }
                if (!dataSourceClassName.isEmpty()) {
                    config.setDataSourceClassName(dataSourceClassName);
                    dataSourceConfigMap = setDataSourcePropertiesMap(dataSourceConfigMap, sqlDatasourceParams.jdbcUrl,
                            sqlDatasourceParams.username, sqlDatasourceParams.password);
                } else {
                    config.setJdbcUrl(sqlDatasourceParams.jdbcUrl);
                }
                String connectionInitSQL = sqlDatasourceParams.options
                        .getStringField(Constants.Options.CONNECTION_INIT_SQL);
                if (!connectionInitSQL.isEmpty()) {
                    config.setConnectionInitSql(connectionInitSQL);
                }

                int maximumPoolSize = (int) sqlDatasourceParams.options
                        .getIntField(Constants.Options.MAXIMUM_POOL_SIZE);
                if (maximumPoolSize != -1) {
                    config.setMaximumPoolSize(maximumPoolSize);
                }
                long connectionTimeout = sqlDatasourceParams.options.getIntField(Constants.Options.CONNECTION_TIMEOUT);
                if (connectionTimeout != -1) {
                    config.setConnectionTimeout(connectionTimeout);
                }
                long idleTimeout = sqlDatasourceParams.options.getIntField(Constants.Options.IDLE_TIMEOUT);
                if (idleTimeout != -1) {
                    config.setIdleTimeout(idleTimeout);
                }
                int minimumIdle = (int) sqlDatasourceParams.options.getIntField(Constants.Options.MINIMUM_IDLE);
                if (minimumIdle != -1) {
                    config.setMinimumIdle(minimumIdle);
                }
                long maxLifetime = sqlDatasourceParams.options.getIntField(Constants.Options.MAX_LIFE_TIME);
                if (maxLifetime != -1) {
                    config.setMaxLifetime(maxLifetime);
                }
                long validationTimeout = sqlDatasourceParams.options.getIntField(Constants.Options.VALIDATION_TIMEOUT);
                if (validationTimeout != -1) {
                    config.setValidationTimeout(validationTimeout);
                }
                boolean autoCommit = sqlDatasourceParams.options.getBooleanField(Constants.Options.AUTOCOMMIT);
                config.setAutoCommit(autoCommit);

                if (dataSourceConfigMap != null) {
                    setDataSourceProperties(dataSourceConfigMap, config);
                }
            } else {
                config.setJdbcUrl(sqlDatasourceParams.jdbcUrl);
            }
            hikariDataSource = new HikariDataSource(config);
        } catch (Throwable t) {
            String message = "error in sql connector configuration:" + t.getMessage();
            if (t.getCause() != null) {
                message += ":" + t.getCause().getMessage();
            }
            throw new BallerinaException(message);
        }
    }

    private BMap<String, BRefType> populatePropertiesMap(Map<String, Value> dataSourceConfigMap) {
        if (dataSourceConfigMap == null) {
            return null;
        }
        BMap<String, BRefType> mapProperties = null;
        if (dataSourceConfigMap.size() > 0) {
            mapProperties = new BMap<>();
            for (Map.Entry<String, Value> entry : dataSourceConfigMap.entrySet()) {
                Value propValue = entry.getValue();
                BRefType dataValue = null;
                switch (propValue.getType()) {
                case INT:
                    dataValue = new BInteger(propValue.getIntValue());
                    break;
                case FLOAT:
                    dataValue = new BFloat(propValue.getFloatValue());
                    break;
                case BOOLEAN:
                    dataValue = new BBoolean(propValue.getBooleanValue());
                    break;
                case NULL:
                    break;
                default:
                    dataValue = new BString(propValue.getStringValue());
                }
                mapProperties.put(entry.getKey(), dataValue);
            }
        }
        return mapProperties;
    }

    private BMap<String, BRefType<?>> setDataSourcePropertiesMap(BMap<String, BRefType<?>> dataSourceConfigMap,
            String jdbcurl, String username, String password) {
        if (dataSourceConfigMap != null) {
            if (!dataSourceConfigMap.hasKey(Constants.URL)) {
                dataSourceConfigMap.put(Constants.URL, new BString(jdbcurl));
            }
        } else {
            dataSourceConfigMap = new BMap<>();
            dataSourceConfigMap.put(Constants.URL, new BString(jdbcurl));
        }
        dataSourceConfigMap.put(Constants.USER, new BString(username));
        dataSourceConfigMap.put(Constants.PASSWORD, new BString(password));
        return dataSourceConfigMap;
    }

    private String constructJDBCURL(String dbType, String hostOrPath, int port, String dbName, String username,
            String password, String dbOptions) {
        StringBuilder jdbcUrl = new StringBuilder();
        dbType = dbType.toUpperCase(Locale.ENGLISH);
        hostOrPath = hostOrPath.replaceAll("/$", "");
        switch (dbType) {
        case Constants.DBTypes.MYSQL:
            if (port <= 0) {
                port = Constants.DefaultPort.MYSQL;
            }
            jdbcUrl.append("jdbc:mysql://").append(hostOrPath).append(":").append(port).append("/").append(dbName);
            break;
        case Constants.DBTypes.SQLSERVER:
            if (port <= 0) {
                port = Constants.DefaultPort.SQLSERVER;
            }
            jdbcUrl.append("jdbc:sqlserver://").append(hostOrPath).append(":").append(port).append(";databaseName=")
                    .append(dbName);
            break;
        case Constants.DBTypes.ORACLE:
            if (port <= 0) {
                port = Constants.DefaultPort.ORACLE;
            }
            jdbcUrl.append("jdbc:oracle:thin:").append(username).append("/").append(password).append("@")
                    .append(hostOrPath).append(":").append(port).append("/").append(dbName);
            break;
        case Constants.DBTypes.SYBASE:
            if (port <= 0) {
                port = Constants.DefaultPort.SYBASE;
            }
            jdbcUrl.append("jdbc:sybase:Tds:").append(hostOrPath).append(":").append(port).append("/").append(dbName);
            break;
        case Constants.DBTypes.POSTGRESQL:
            if (port <= 0) {
                port = Constants.DefaultPort.POSTGRES;
            }
            jdbcUrl.append("jdbc:postgresql://").append(hostOrPath).append(":").append(port).append("/").append(dbName);
            break;
        case Constants.DBTypes.IBMDB2:
            if (port <= 0) {
                port = Constants.DefaultPort.IBMDB2;
            }
            jdbcUrl.append("jdbc:db2:").append(hostOrPath).append(":").append(port).append("/").append(dbName);
            break;
        case Constants.DBTypes.HSQLDB_SERVER:
            if (port <= 0) {
                port = Constants.DefaultPort.HSQLDB_SERVER;
            }
            jdbcUrl.append("jdbc:hsqldb:hsql://").append(hostOrPath).append(":").append(port).append("/")
                    .append(dbName);
            break;
        case Constants.DBTypes.HSQLDB_FILE:
            jdbcUrl.append("jdbc:hsqldb:file:").append(hostOrPath).append(File.separator).append(dbName);
            break;
        case Constants.DBTypes.H2_SERVER:
            if (port <= 0) {
                port = Constants.DefaultPort.H2_SERVER;
            }
            jdbcUrl.append("jdbc:h2:tcp:").append(hostOrPath).append(":").append(port).append("/").append(dbName);
            break;
        case Constants.DBTypes.H2_FILE:
            jdbcUrl.append("jdbc:h2:file:").append(hostOrPath).append(File.separator).append(dbName);
            break;
        case Constants.DBTypes.H2_MEMORY:
            jdbcUrl.append("jdbc:h2:mem:").append(dbName);
            break;
        case Constants.DBTypes.DERBY_SERVER:
            if (port <= 0) {
                port = Constants.DefaultPort.DERBY_SERVER;
            }
            jdbcUrl.append("jdbc:derby:").append(hostOrPath).append(":").append(port).append("/").append(dbName);
            break;
        case Constants.DBTypes.DERBY_FILE:
            jdbcUrl.append("jdbc:derby:").append(hostOrPath).append(File.separator).append(dbName);
            break;
        default:
            throw new BallerinaException("cannot generate url for unknown database type : " + dbType);
        }
        // Set peer address
        peerAddress = hostOrPath + ":" + port;
        return dbOptions.isEmpty() ? jdbcUrl.toString() : jdbcUrl.append(dbOptions).toString();
    }

    private String getXADatasourceClassName(String dbType, String url, String userName, String password) {
        String xaDataSource = null;
        switch (dbType) {
        case Constants.DBTypes.MYSQL:
            int driverMajorVersion;
            try (Connection conn = DriverManager.getConnection(url, userName, password)) {
                driverMajorVersion = conn.getMetaData().getDriverMajorVersion();
                if (driverMajorVersion == 5) {
                    xaDataSource = Constants.XADataSources.MYSQL_5_XA_DATASOURCE;
                } else if (driverMajorVersion > 5) {
                    xaDataSource = Constants.XADataSources.MYSQL_6_XA_DATASOURCE;
                }
            } catch (SQLException e) {
                throw new BallerinaException(
                        "error in get connection: " + Constants.CONNECTOR_NAME + ": " + e.getMessage(), e);
            }
            break;
        case Constants.DBTypes.SQLSERVER:
            xaDataSource = Constants.XADataSources.SQLSERVER_XA_DATASOURCE;
            break;
        case Constants.DBTypes.ORACLE:
            xaDataSource = Constants.XADataSources.ORACLE_XA_DATASOURCE;
            break;
        case Constants.DBTypes.SYBASE:
            xaDataSource = Constants.XADataSources.SYBASE_XA_DATASOURCE;
            break;
        case Constants.DBTypes.POSTGRESQL:
            xaDataSource = Constants.XADataSources.POSTGRES_XA_DATASOURCE;
            break;
        case Constants.DBTypes.IBMDB2:
            xaDataSource = Constants.XADataSources.IBMDB2_XA_DATASOURCE;
            break;
        case Constants.DBTypes.HSQLDB:
        case Constants.DBTypes.HSQLDB_SERVER:
        case Constants.DBTypes.HSQLDB_FILE:
            xaDataSource = Constants.XADataSources.HSQLDB_XA_DATASOURCE;
            break;
        case Constants.DBTypes.H2:
        case Constants.DBTypes.H2_SERVER:
        case Constants.DBTypes.H2_FILE:
        case Constants.DBTypes.H2_MEMORY:
            xaDataSource = Constants.XADataSources.H2_XA_DATASOURCE;
            break;
        case Constants.DBTypes.DERBY_SERVER:
            xaDataSource = Constants.XADataSources.DERBY_SERVER_XA_DATASOURCE;
            break;
        case Constants.DBTypes.DERBY_FILE:
            xaDataSource = Constants.XADataSources.DERBY_FILE_XA_DATASOURCE;
            break;
        default:
            throw new BallerinaException("unknown database type used for xa connection : " + dbType);
        }
        return xaDataSource;
    }

    private void setDataSourceProperties(BMap<String, BRefType<?>> options, HikariConfig config) {
        for (String key : options.keys()) {
            BValue value = options.get(key);
            if (value instanceof BString) {
                config.addDataSourceProperty(key, value.stringValue());
            } else if (value instanceof BInteger) {
                config.addDataSourceProperty(key, ((BInteger) value).intValue());
            } else if (value instanceof BBoolean) {
                config.addDataSourceProperty(key, ((BBoolean) value).booleanValue());
            } else if (value instanceof BFloat) {
                config.addDataSourceProperty(key, ((BFloat) value).floatValue());
            }
        }
    }

    @Override
    public String stringValue() {
        return null;
    }

    @Override
    public BType getType() {
        return null;
    }

    @Override
    public void stamp(BType type) {

    }

    @Override
    public BValue copy(Map<BValue, BValue> refs) {
        return null;
    }

    private boolean isXADataSource() {
        try {
            return hikariDataSource.isWrapperFor(XADataSource.class);
        } catch (SQLException e) {
            throw new BallerinaException("error in check distributed data source: " + e.getCause().getMessage());
        }
    }

    /**
     * This class encapsulates the parameters required for the initialization of {@code SQLDatasource} class.
     */
    protected static class SQLDatasourceParams {
         private Struct options;
         private String jdbcUrl;
         private String dbType;
         private String hostOrPath;
         private int port;
         private String username;
         private String password;
         private String dbName;
         private String urlOptions;
         private Map dbOptionsMap;

        private SQLDatasourceParams(SQLDatasourceParamsBuilder builder) {
            this.options = builder.options;
            this.jdbcUrl = builder.jdbcUrl;
            this.dbType = builder.dbType;
            this.hostOrPath = builder.hostOrPath;
            this.port = builder.port;
            this.username = builder.username;
            this.password = builder.password;
            this.dbName = builder.dbName;
            this.urlOptions = builder.urlOptions;
            this.dbOptionsMap = builder.dbOptionsMap;
        }
    }

    /**
     * Builder class for SQLDatasourceParams class.
     */
    public static class SQLDatasourceParamsBuilder {
        private Struct options;
        private String jdbcUrl;
        private String dbType;
        private String hostOrPath;
        private int port;
        private String username;
        private String password;
        private String dbName;
        private String urlOptions;
        private Map<String, Value> dbOptionsMap;

        public SQLDatasourceParamsBuilder(String dbType) {
            this.dbType = dbType;
        }

        public SQLDatasourceParams build() {
            return new SQLDatasourceParams(this);
        }

        public SQLDatasourceParamsBuilder withJdbcUrl(String jdbcUrl) {
            this.jdbcUrl = jdbcUrl;
            return this;
        }

        public SQLDatasourceParamsBuilder withDbType(String dbType) {
            this.dbType = dbType;
            return this;
        }

        public SQLDatasourceParamsBuilder withHostOrPath(String hostOrPath) {
            this.hostOrPath = hostOrPath;
            return this;
        }

        public SQLDatasourceParamsBuilder withPort(int port) {
            this.port = port;
            return this;
        }

        public SQLDatasourceParamsBuilder withUsername(String username) {
            this.username = username;
            return this;
        }

        public SQLDatasourceParamsBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        public SQLDatasourceParamsBuilder withUrlOptions(String urlOptions) {
            this.urlOptions = urlOptions;
            return this;
        }

        public SQLDatasourceParamsBuilder withDbOptionsMap(Map<String, Value> dbOptionsMap) {
            this.dbOptionsMap = dbOptionsMap;
            return this;
        }

        public SQLDatasourceParamsBuilder withOptions(Struct options) {
            this.options = options;
            return this;
        }

        public SQLDatasourceParamsBuilder withDbName(String dbName) {
            this.dbName = dbName;
            return this;
        }
    }
}
