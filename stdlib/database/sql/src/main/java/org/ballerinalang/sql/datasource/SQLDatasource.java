/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.sql.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.ballerinalang.jvm.values.DecimalValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.sql.Constants;
import org.ballerinalang.sql.utils.ErrorGenerator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * SQL datasource representation.
 *
 * @since 1.2.0
 */
public class SQLDatasource {

    private HikariDataSource hikariDataSource;
    private AtomicInteger clientCounter = new AtomicInteger(0);
    private Lock mutex = new ReentrantLock();
    private boolean poolShutdown = false;

    private SQLDatasource(SQLDatasourceParams sqlDatasourceParams) {
        buildDataSource(sqlDatasourceParams);
        try (Connection con = getSQLConnection()) {
        } catch (SQLException e) {
            throw ErrorGenerator.getSQLDatabaseError(e,
                    "error while obtaining connection for " + Constants.CONNECTOR_NAME + ", ");
        }
    }

    /**
     * Retrieve the {@link SQLDatasource}} object corresponding to the provided  URL in
     * {@link SQLDatasource.SQLDatasourceParams}.
     * Creates a datasource if it doesn't exist.
     *
     * @param sqlDatasourceParams datasource parameters required to retrieve the JDBC URL for datasource lookup and
     *                            initialization of the newly created datasource if it doesn't exists
     * @return The existing or newly created {@link SQLDatasource} object
     */
    public static SQLDatasource retrieveDatasource(SQLDatasource.SQLDatasourceParams sqlDatasourceParams) {
        PoolKey poolKey = new PoolKey(sqlDatasourceParams.url, sqlDatasourceParams.options);
        Map<PoolKey, SQLDatasource> hikariDatasourceMap = SQLDatasourceUtils
                .retrieveDatasourceContainer(sqlDatasourceParams.connectionPool);
        // map could be null only in a local pool creation scenario
        if (hikariDatasourceMap == null) {
            hikariDatasourceMap = SQLDatasourceUtils.putDatasourceContainer(sqlDatasourceParams.connectionPool,
                    new ConcurrentHashMap<>());
        }
        SQLDatasource existingSqlDatasource = hikariDatasourceMap.get(poolKey);
        SQLDatasource sqlDatasourceToBeReturned = existingSqlDatasource;
        if (existingSqlDatasource != null) {
            existingSqlDatasource.acquireMutex();
            try {
                if (!existingSqlDatasource.isPoolShutdown()) {
                    existingSqlDatasource.incrementClientCounter();
                } else {
                    sqlDatasourceToBeReturned = hikariDatasourceMap.compute(poolKey,
                            (key, value) -> createAndInitDatasource(sqlDatasourceParams));
                }
            } finally {
                existingSqlDatasource.releaseMutex();
            }
        } else {
            sqlDatasourceToBeReturned = hikariDatasourceMap.computeIfAbsent(poolKey,
                    key -> createAndInitDatasource(sqlDatasourceParams));

        }
        return sqlDatasourceToBeReturned;
    }

    private static SQLDatasource createAndInitDatasource(SQLDatasource.SQLDatasourceParams sqlDatasourceParams) {
        SQLDatasource newSqlDatasource = new SQLDatasource(sqlDatasourceParams);
        newSqlDatasource.incrementClientCounter();
        return newSqlDatasource;
    }


    public Connection getSQLConnection() throws SQLException {
        return hikariDataSource.getConnection();
    }

    private void closeConnectionPool() {
        hikariDataSource.close();
        poolShutdown = true;
    }

    private boolean isPoolShutdown() {
        return poolShutdown;
    }

    private void incrementClientCounter() {
        clientCounter.incrementAndGet();
    }

    public void decrementClientCounterAndAttemptPoolShutdown() {
        acquireMutex();
        if (!poolShutdown) {
            if (clientCounter.decrementAndGet() == 0) {
                closeConnectionPool();
            }
        }
        releaseMutex();
    }

    private void releaseMutex() {
        mutex.unlock();
    }

    private void acquireMutex() {
        mutex.lock();
    }

    private void buildDataSource(SQLDatasourceParams sqlDatasourceParams) {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(sqlDatasourceParams.url);
            config.setUsername(sqlDatasourceParams.user);
            config.setPassword(sqlDatasourceParams.password);
            if (sqlDatasourceParams.datasourceName != null && !sqlDatasourceParams.datasourceName.isEmpty()) {
                if (sqlDatasourceParams.options == null || !sqlDatasourceParams.options
                        .containsKey(Constants.Options.URL)) {
                    //It is required to set the url to the datasource property when the
                    //datasource class name is provided. Because according to hikari
                    //either jdbcUrl or datasourceClassName will be honoured.
                    config.addDataSourceProperty(Constants.Options.URL, sqlDatasourceParams.url);
                }
            }
            config.setDataSourceClassName(sqlDatasourceParams.datasourceName);
            if (sqlDatasourceParams.connectionPool != null) {
                int maxOpenConn = sqlDatasourceParams.connectionPool.
                        getIntValue(Constants.ConnectionPool.MAX_OPEN_CONNECTIONS).intValue();
                if (maxOpenConn > 0) {
                    config.setMaximumPoolSize(maxOpenConn);
                }

                Object connLifeTimeSec = sqlDatasourceParams.connectionPool
                        .get(Constants.ConnectionPool.MAX_CONNECTION_LIFE_TIME_SECONDS);
                if (connLifeTimeSec instanceof DecimalValue) {
                    DecimalValue connLifeTime = (DecimalValue) connLifeTimeSec;
                    if (connLifeTime.floatValue() > 0) {
                        long connLifeTimeMS = Double.valueOf(connLifeTime.floatValue() * 1000).longValue();
                        config.setMaxLifetime(connLifeTimeMS);
                    }
                }
                int minIdleConnections = sqlDatasourceParams.connectionPool
                        .getIntValue(Constants.ConnectionPool.MIN_IDLE_CONNECTIONS).intValue();
                if (minIdleConnections > 0) {
                    config.setMinimumIdle(minIdleConnections);
                }
            }
            if (sqlDatasourceParams.options != null) {
                MapValue<String, Object> optionMap = (MapValue<String, Object>) sqlDatasourceParams.options;
                optionMap.entrySet().forEach(entry -> {
                    if (SQLDatasourceUtils.isSupportedDbOptionType(entry.getValue())) {
                        config.addDataSourceProperty(entry.getKey(), entry.getValue());
                    } else {
                        throw ErrorGenerator.getSQLApplicationError("unsupported type " + entry.getKey()
                                + " for the db option");
                    }
                });
            }
            hikariDataSource = new HikariDataSource(config);
            Runtime.getRuntime().addShutdownHook(new Thread(this::closeConnectionPool));
        } catch (Throwable t) {
            String message = "error in sql connector configuration: " + t.getMessage();
            if (t.getCause() != null) {
                message += ":" + t.getCause().getMessage();
            }
            throw ErrorGenerator.getSQLApplicationError(message);
        }
    }
    /**
     * This class encapsulates the parameters required for the initialization of {@code SQLDatasource} class.
     */
    public static class SQLDatasourceParams {
        private String url;
        private String user;
        private String password;
        private String datasourceName;
        private MapValue connectionPool;
        private MapValue options;

        public SQLDatasourceParams() {
        }

        public SQLDatasourceParams setConnectionPool(MapValue connectionPool) {
            this.connectionPool = connectionPool;
            return this;
        }

        public SQLDatasourceParams setUrl(String url) {
            this.url = url;
            return this;
        }

        public SQLDatasourceParams setUser(String user) {
            this.user = user;
            return this;
        }

        public SQLDatasourceParams setPassword(String password) {
            this.password = password;
            return this;
        }

        public SQLDatasourceParams setDatasourceName(String datasourceName) {
            this.datasourceName = datasourceName;
            return this;
        }

        public SQLDatasourceParams setOptions(MapValue options) {
            this.options = options;
            return this;
        }
    }
}
