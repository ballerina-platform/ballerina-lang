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
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.XADataSource;

/**
 * Native SQL Connector.
 *
 * @since 0.8.0
 */
public class SQLDatasource {

    private HikariDataSource hikariDataSource;
    private String peerAddress;
    private String databaseName;
    private String databaseProductName;
    private boolean xaConn;
    private boolean globalDatasource;
    private AtomicInteger clientCounter = new AtomicInteger(0);
    private Semaphore mutex = new Semaphore(1);
    private boolean poolShutdown = false;

    public SQLDatasource init(SQLDatasourceParams sqlDatasourceParams) {
        this.globalDatasource = sqlDatasourceParams.isGlobalDatasource;
        databaseName = sqlDatasourceParams.dbName;
        peerAddress = sqlDatasourceParams.jdbcUrl;
        buildDataSource(sqlDatasourceParams);
        xaConn = isXADataSource();
        try (Connection con = getSQLConnection()) {
            databaseProductName = con.getMetaData().getDatabaseProductName().toLowerCase(Locale.ENGLISH);
        } catch (SQLException e) {
            throw new BallerinaException("error in get connection: " + Constants.CONNECTOR_NAME + ": " + e.getMessage(),
                    e);
        }
        return this;
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

    public Connection getSQLConnection() throws SQLException {
        return hikariDataSource.getConnection();
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
        poolShutdown = true;
    }

    public boolean isGlobalDatasource() {
        return globalDatasource;
    }

    public boolean isPoolShutdown() {
        return poolShutdown;
    }

    public void incrementClientCounter() {
        clientCounter.incrementAndGet();
    }

    public void decrementClientCounterAndAttemptPoolShutdown() throws InterruptedException {
        acquireMutex();
        if (!poolShutdown) {
            if (clientCounter.decrementAndGet() == 0) {
                closeConnectionPool();
            }
        }
        releaseMutex();
    }

    public void acquireMutex() throws InterruptedException {
        mutex.acquire();
    }

    public void releaseMutex() {
        mutex.release();
    }

    private void buildDataSource(SQLDatasourceParams sqlDatasourceParams) {
        try {
            HikariConfig config = new HikariConfig();
            //Set username password
            config.setUsername(sqlDatasourceParams.username);
            config.setPassword(sqlDatasourceParams.password);
            //Set optional properties
            if (sqlDatasourceParams.poolOptionsWrapper != null) {
                boolean isXA = sqlDatasourceParams.poolOptionsWrapper.getBoolean(Constants.Options.IS_XA);

                String dataSourceClassName = sqlDatasourceParams.poolOptionsWrapper
                        .getString(Constants.Options.DATASOURCE_CLASSNAME);
                if (isXA && dataSourceClassName.isEmpty()) {
                    dataSourceClassName = getXADatasourceClassName(sqlDatasourceParams.dbType,
                            sqlDatasourceParams.jdbcUrl, sqlDatasourceParams.username, sqlDatasourceParams.password);
                }
                if (!dataSourceClassName.isEmpty()) {
                    config.setDataSourceClassName(dataSourceClassName);
                    if (sqlDatasourceParams.dbOptionsMap == null || !sqlDatasourceParams.dbOptionsMap
                            .containsKey(Constants.URL)) {
                        config.addDataSourceProperty(Constants.URL, sqlDatasourceParams.jdbcUrl);
                    }
                    config.addDataSourceProperty(Constants.USER, sqlDatasourceParams.username);
                    config.addDataSourceProperty(Constants.PASSWORD, sqlDatasourceParams.password);
                } else {
                    config.setJdbcUrl(sqlDatasourceParams.jdbcUrl);
                }
                String connectionInitSQL = sqlDatasourceParams.poolOptionsWrapper
                        .getString(Constants.Options.CONNECTION_INIT_SQL);
                if (!connectionInitSQL.isEmpty()) {
                    config.setConnectionInitSql(connectionInitSQL);
                }

                int maximumPoolSize = sqlDatasourceParams.poolOptionsWrapper
                        .getInt(Constants.Options.MAXIMUM_POOL_SIZE).intValue();
                if (maximumPoolSize != -1) {
                    config.setMaximumPoolSize(maximumPoolSize);
                }
                long connectionTimeout = (sqlDatasourceParams.poolOptionsWrapper
                        .getInt(Constants.Options.CONNECTION_TIMEOUT));
                if (connectionTimeout != -1) {
                    config.setConnectionTimeout(connectionTimeout);
                }
                long idleTimeout = sqlDatasourceParams.poolOptionsWrapper.getInt(Constants.Options.IDLE_TIMEOUT);
                if (idleTimeout != -1) {
                    config.setIdleTimeout(idleTimeout);
                }
                int minimumIdle = sqlDatasourceParams.poolOptionsWrapper
                        .getInt(Constants.Options.MINIMUM_IDLE).intValue();
                if (minimumIdle != -1) {
                    config.setMinimumIdle(minimumIdle);
                }
                long maxLifetime = (sqlDatasourceParams.poolOptionsWrapper.getInt(Constants.Options.MAX_LIFE_TIME));
                if (maxLifetime != -1) {
                    config.setMaxLifetime(maxLifetime);
                }
                long validationTimeout = sqlDatasourceParams.poolOptionsWrapper
                        .getInt(Constants.Options.VALIDATION_TIMEOUT);
                if (validationTimeout != -1) {
                    config.setValidationTimeout(validationTimeout);
                }
                boolean autoCommit = sqlDatasourceParams.poolOptionsWrapper.getBoolean(Constants.Options.AUTOCOMMIT);
                config.setAutoCommit(autoCommit);
            } else {
                config.setJdbcUrl(sqlDatasourceParams.jdbcUrl);
            }
            if (sqlDatasourceParams.dbOptionsMap != null) {
                sqlDatasourceParams.dbOptionsMap.forEach((key, value) -> {
                    if (SQLDatasourceUtils.isSupportedDbOptionType(value)) {
                        config.addDataSourceProperty(key, value);
                    } else {
                        throw new BallerinaException("Unsupported type for the db option: " + key);
                    }
                });
            }
            hikariDataSource = new HikariDataSource(config);
            Runtime.getRuntime().addShutdownHook(new Thread(this::closeConnectionPool));
        } catch (Throwable t) {
            String message = "error in sql connector configuration:" + t.getMessage();
            if (t.getCause() != null) {
                message += ":" + t.getCause().getMessage();
            }
            throw new BallerinaException(message);
        }
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
        private PoolOptionsWrapper poolOptionsWrapper;
        private String jdbcUrl;
        private String dbType;
        private String username;
        private String password;
        private String dbName;
        private boolean isGlobalDatasource;
        private MapValue<String, Object> dbOptionsMap;

        private SQLDatasourceParams(SQLDatasourceParamsBuilder builder) {
            this.poolOptionsWrapper = builder.poolOptions;
            this.jdbcUrl = builder.jdbcUrl;
            this.dbType = builder.dbType;
            this.username = builder.username;
            this.password = builder.password;
            this.dbName = builder.dbName;
            this.isGlobalDatasource = builder.isGlobalDatasource;
            this.dbOptionsMap = builder.dbOptionsMap;
        }

        public String getJdbcUrl() {
            return jdbcUrl;
        }

        public PoolOptionsWrapper getPoolOptionsWrapper() {
            return poolOptionsWrapper;
        }
    }

    /**
     * Builder class for SQLDatasourceParams class.
     */
    public static class SQLDatasourceParamsBuilder {
        private PoolOptionsWrapper poolOptions;
        private String jdbcUrl;
        private String dbType;
        private String username;
        private String password;
        private String dbName;
        private boolean isGlobalDatasource;
        private MapValue<String, Object> dbOptionsMap;

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

        public SQLDatasourceParamsBuilder withUsername(String username) {
            this.username = username;
            return this;
        }

        public SQLDatasourceParamsBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        public SQLDatasourceParamsBuilder withDbOptionsMap(MapValue<String, Object> dbOptionsMap) {
            this.dbOptionsMap = dbOptionsMap;
            return this;
        }

        public SQLDatasourceParamsBuilder withPoolOptions(PoolOptionsWrapper options) {
            this.poolOptions = options;
            return this;
        }

        public SQLDatasourceParamsBuilder withDbName(String dbName) {
            this.dbName = dbName;
            return this;
        }

        public SQLDatasourceParamsBuilder withIsGlobalDatasource(boolean isGlobalDatasource) {
            this.isGlobalDatasource = isGlobalDatasource;
            return this;
        }
    }
}
