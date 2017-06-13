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
package org.ballerinalang.nativeimpl.actions.data.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import javax.sql.XADataSource;

/**
 * Native SQL Connector.
 *
 * @since 0.8.0
 */
public class SQLDatasource implements BValue {

    static final String CONNECTOR_PACKAGE = "ballerina.data.sql";

    private HikariDataSource hikariDataSource;
    private String databaseName;
    private String connectorId;
    private boolean xaConn;

    public String getDatabaseName() {
        return databaseName;
    }

    public SQLDatasource() {}

    public boolean init(BMap options) {
        buildDataSource(options);
        connectorId = UUID.randomUUID().toString();
        xaConn = isXADataSource();
        try (Connection con = getSQLConnection()) {
            databaseName = con.getMetaData().getDatabaseProductName().toLowerCase(Locale.ENGLISH);
        } catch (SQLException e) {
            throw new BallerinaException(
                    "error in get connection: " + Constants.CONNECTOR_NAME + ": " + e.getMessage(), e);
        }
        return true;
    }

    public Connection getSQLConnection() {
        try {
           return  hikariDataSource.getConnection();
        } catch (SQLException e) {
            throw new BallerinaException(
                    "error in get connection: " + Constants.CONNECTOR_NAME + ": " + e.getMessage(), e);
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

    private void buildDataSource(BMap options) {
        try {
            HikariConfig config = new HikariConfig();
            String key = Constants.PoolProperties.DATA_SOURCE_CLASSNAME;
            BValue value = options.get(key);
            if (value != null) {
                config.setDataSourceClassName(value.stringValue());
            }
            key = Constants.PoolProperties.JDBC_URL;
            value = options.get(key);
            if (value != null) {
                config.setJdbcUrl(value.stringValue());
            }
            key = Constants.PoolProperties.USER_NAME;
            value = options.get(key);
            if (value != null) {
                config.setUsername(value.stringValue());
            }
            key = Constants.PoolProperties.PASSWORD;
            value = options.get(key);
            if (value != null) {
                config.setPassword(value.stringValue());
            }
            key = Constants.PoolProperties.AUTO_COMMIT;
            value = options.get(key);
            if (value != null) {
                config.setAutoCommit(Boolean.parseBoolean(value.stringValue()));
            }
            key = Constants.PoolProperties.CONNECTION_TIMEOUT;
            value = options.get(key);
            if (value != null) {
                config.setConnectionTimeout(Long.parseLong(value.stringValue()));
            }
            key = Constants.PoolProperties.IDLE_TIMEOUT;
            value = options.get(key);
            if (value != null) {
                config.setIdleTimeout(Long.parseLong(value.stringValue()));
            }
            key = Constants.PoolProperties.MAX_LIFETIME;
            value = options.get(key);
            if (value != null) {
                config.setMaxLifetime(Long.parseLong(value.stringValue()));
            }
            key = Constants.PoolProperties.CONNECTION_TEST_QUERY;
            value = options.get(key);
            if (value != null) {
                config.setConnectionTestQuery(value.stringValue());
            }
            key = Constants.PoolProperties.MINIMUM_IDLE;
            value = options.get(key);
            if (value != null) {
                config.setMinimumIdle(Integer.parseInt(value.stringValue()));
            }
            key = Constants.PoolProperties.MAXIMUM_POOL_SIZE;
            value = options.get(key);
            if (value != null) {
                config.setMaximumPoolSize(Integer.parseInt(value.stringValue()));
            }
            key = Constants.PoolProperties.POOL_NAME;
            value = options.get(key);
            if (value != null) {
                config.setPoolName(value.stringValue());
            }
            key = Constants.PoolProperties.ISOLATE_INTERNAL_QUERIES;
            value = options.get(key);
            if (value != null) {
                config.setIsolateInternalQueries(Boolean.parseBoolean(value.stringValue()));
            }
            key = Constants.PoolProperties.ALLOW_POOL_SUSPENSION;
            value = options.get(key);
            if (value != null) {
                config.setAllowPoolSuspension(Boolean.parseBoolean(value.stringValue()));
            }
            key = Constants.PoolProperties.READ_ONLY;
            value = options.get(key);
            if (value != null) {
                config.setReadOnly(Boolean.parseBoolean(value.stringValue()));
            }
            key = Constants.PoolProperties.REGISTER_MBEANS;
            value = options.get(key);
            if (value != null) {
                config.setRegisterMbeans(Boolean.parseBoolean(value.stringValue()));
            }
            key = Constants.PoolProperties.CATALOG;
            value = options.get(key);
            if (value != null) {
                config.setCatalog(value.stringValue());
            }
            key = Constants.PoolProperties.CONNECTION_INIT_SQL;
            value = options.get(key);
            if (value != null) {
                config.setConnectionInitSql(value.stringValue());
            }
            key = Constants.PoolProperties.DRIVER_CLASSNAME;
            value = options.get(key);
            if (value != null) {
                config.setDriverClassName(value.stringValue());
            }
            key = Constants.PoolProperties.TRANSACTION_ISOLATION;
            value = options.get(key);
            if (value != null) {
                config.setTransactionIsolation(value.stringValue());
            }
            key = Constants.PoolProperties.VALIDATION_TIMEOUT;
            value = options.get(key);
            if (value != null) {
                config.setValidationTimeout(Long.parseLong(value.stringValue()));
            }
            key = Constants.PoolProperties.LEAK_DETECTION_THRESHOLD;
            value = options.get(key);
            if (value != null) {
                config.setLeakDetectionThreshold(Long.parseLong(value.stringValue()));
            }
            setDataSourceProperties(options, config);
            hikariDataSource = new HikariDataSource(config);
        } catch (Throwable t) {
            String errorMessage = "error in sql connector configuration";
            if (t.getCause() != null) {
                throw new BallerinaException(errorMessage + ": " + t.getCause().getMessage());
            } else {
                throw new BallerinaException(errorMessage);
            }
        }
    }

    private void setDataSourceProperties(BMap options, HikariConfig config) {
        Set<String> keySet = options.keySet();
        for (String key : keySet) {
            String keyName = key;
            if (keyName.startsWith(Constants.PoolProperties.DATASOURCE)) {
                String keyValue = keyName.substring(Constants.PoolProperties.DATASOURCE.length());
                BValue value = options.get(key);
                if (value instanceof BString) {
                    config.addDataSourceProperty(keyValue, value.stringValue());
                } else if (value instanceof BInteger) {
                    config.addDataSourceProperty(keyValue, ((BInteger) value).intValue());
                } else if (value instanceof BBoolean) {
                    config.addDataSourceProperty(keyValue, Boolean.parseBoolean(value.stringValue()));
                }
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
    public BValue copy() {
        return null;
    }
    
    private boolean isXADataSource() {
        try {
            if (hikariDataSource.isWrapperFor(XADataSource.class)) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            throw new BallerinaException("error in check distributed data source: " + e.getCause().getMessage());
        }
    }
}
