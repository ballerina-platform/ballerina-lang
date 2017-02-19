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
package org.ballerinalang.nativeimpl.connectors.data.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.ballerinalang.model.SymbolScope;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BLong;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaConnector;
import org.ballerinalang.natives.connectors.AbstractNativeConnector;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.osgi.service.component.annotations.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

/**
 * Native SQL Connector.
 *
 * @since 0.8.0
 */
@BallerinaConnector(
        packageName = SQLConnector.CONNECTOR_PACKAGE,
        connectorName = SQLConnector.CONNECTOR_NAME,
        args = {@Argument(name = "options", type = TypeEnum.MAP)})
@Component(
        name = "ballerina.data.connectors.sql",
        immediate = true,
        service = AbstractNativeConnector.class)
public class SQLConnector extends AbstractNativeConnector {

    public static final String CONNECTOR_PACKAGE = "ballerina.data.sql";
    public static final String CONNECTOR_NAME = "ClientConnector";

    private HikariDataSource hikariDataSource;

    public SQLConnector(SymbolScope enclosingScope) {
        super(enclosingScope);
    }

    @Override
    public boolean init(BValue[] bValueRefs) {
        BMap options = (BMap) bValueRefs[0];
        buildDataSource(options);
        return true;
    }

    @Override
    public SQLConnector getInstance() {
        return new SQLConnector(symbolScope);
    }

    public Connection getSQLConnection() {
        try {
           return  hikariDataSource.getConnection();
        } catch (SQLException e) {
            throw new BallerinaException(
                    "error in get connection: " + SQLConnector.CONNECTOR_NAME + ": " + e.getMessage(), e);
        }
    }

    private void buildDataSource(BMap options) {
        HikariConfig config = new HikariConfig();
        BString key = new BString(Constants.PoolProperties.DATA_SOURCE_CLASSNAME);
        BValue value = options.get(key);
        if (value != null) {
            config.setDataSourceClassName(value.stringValue());
            options.remove(key);
        }
        key = new BString(Constants.PoolProperties.JDBC_URL);
        value = options.get(key);
        if (value != null) {
            config.setJdbcUrl(value.stringValue());
            options.remove(key);
        }
        key = new BString(Constants.PoolProperties.USER_NAME);
        value = options.get(key);
        if (value != null) {
            config.setUsername(value.stringValue());
            options.remove(key);
        }
        key = new BString(Constants.PoolProperties.PASSWORD);
        value = options.get(key);
        if (value != null) {
            config.setPassword(value.stringValue());
            options.remove(key);
        }
        key = new BString(Constants.PoolProperties.AUTO_COMMIT);
        value = options.get(key);
        if (value != null) {
            config.setAutoCommit(Boolean.parseBoolean(value.stringValue()));
            options.remove(key);
        }
        key = new BString(Constants.PoolProperties.CONNECTION_TIMEOUT);
        value = options.get(key);
        if (value != null) {
            config.setConnectionTimeout(Long.parseLong(value.stringValue()));
            options.remove(key);
        }
        key = new BString(Constants.PoolProperties.IDLE_TIMEOUT);
        value = options.get(key);
        if (value != null) {
            config.setIdleTimeout(Long.parseLong(value.stringValue()));
            options.remove(key);
        }
        key = new BString(Constants.PoolProperties.MAX_LIFETIME);
        value = options.get(key);
        if (value != null) {
            config.setMaxLifetime(Long.parseLong(value.stringValue()));
            options.remove(key);
        }
        key = new BString(Constants.PoolProperties.CONNECTION_TEST_QUERY);
        value = options.get(key);
        if (value != null) {
            config.setConnectionTestQuery(value.stringValue());
            options.remove(key);
        }
        key = new BString(Constants.PoolProperties.MINIMUM_IDLE);
        value = options.get(key);
        if (value != null) {
            config.setMinimumIdle(Integer.parseInt(value.stringValue()));
            options.remove(key);
        }
        key = new BString(Constants.PoolProperties.MAXIMUM_POOL_SIZE);
        value = options.get(key);
        if (value != null) {
            config.setMaximumPoolSize(Integer.parseInt(value.stringValue()));
            options.remove(key);
        }
        key = new BString(Constants.PoolProperties.POOOL_NAME);
        value = options.get(key);
        if (value != null) {
            config.setPoolName(value.stringValue());
            options.remove(key);
        }
        key = new BString(Constants.PoolProperties.ISOLATE_INTERNAL_QUERIES);
        value = options.get(key);
        if (value != null) {
            config.setIsolateInternalQueries(Boolean.parseBoolean(value.stringValue()));
            options.remove(key);
        }
        key = new BString(Constants.PoolProperties.ALLOW_POOL_SUSPENSION);
        value = options.get(key);
        if (value != null) {
            config.setAllowPoolSuspension(Boolean.parseBoolean(value.stringValue()));
            options.remove(key);
        }
        key = new BString(Constants.PoolProperties.READ_ONLY);
        value = options.get(key);
        if (value != null) {
            config.setReadOnly(Boolean.parseBoolean(value.stringValue()));
            options.remove(key);
        }
        key = new BString(Constants.PoolProperties.REGISTER_MBEANS);
        value = options.get(key);
        if (value != null) {
            config.setRegisterMbeans(Boolean.parseBoolean(value.stringValue()));
            options.remove(key);
        }
        key = new BString(Constants.PoolProperties.CATALOG);
        value = options.get(key);
        if (value != null) {
            config.setCatalog(value.stringValue());
            options.remove(key);
        }
        key = new BString(Constants.PoolProperties.CONNECTION_INIT_SQL);
        value = options.get(key);
        if (value != null) {
            config.setConnectionInitSql(value.stringValue());
            options.remove(key);
        }
        key = new BString(Constants.PoolProperties.DRIVER_CLASSNAME);
        value = options.get(key);
        if (value != null) {
            config.setDriverClassName(value.stringValue());
            options.remove(key);
        }
        key = new BString(Constants.PoolProperties.TRANSACTION_ISOLATION);
        value = options.get(key);
        if (value != null) {
            config.setTransactionIsolation(value.stringValue());
            options.remove(key);
        }
        key = new BString(Constants.PoolProperties.VALIDATION_TIMEOUT);
        value = options.get(key);
        if (value != null) {
            config.setValidationTimeout(Long.parseLong(value.stringValue()));
            options.remove(key);
        }
        key = new BString(Constants.PoolProperties.LEAK_DETECTION_THRESHOLD);
        value = options.get(key);
        if (value != null) {
            config.setLeakDetectionThreshold(Long.parseLong(value.stringValue()));
            options.remove(key);
        }
        setDataSourceProperties(options, config);
        hikariDataSource = new HikariDataSource(config);
    }

    void setDataSourceProperties(BMap options, HikariConfig config) {
        Set<BString> keySet = options.keySet();
        for (BString key : keySet) {
            String keyName = key.stringValue();
            if (keyName.startsWith(Constants.PoolProperties.DATASOURCE)) {
                String keyValue = keyName.substring(Constants.PoolProperties.DATASOURCE.length());
                BValue value = options.get(key);
                if (value instanceof BString) {
                    config.addDataSourceProperty(keyValue, value.stringValue());
                } else if (value instanceof BInteger) {
                    config.addDataSourceProperty(keyValue, Integer.parseInt(value.stringValue()));
                } else if (value instanceof BLong) {
                    config.addDataSourceProperty(keyValue, Long.parseLong(value.stringValue()));
                } else if (value instanceof BBoolean) {
                    config.addDataSourceProperty(keyValue, Boolean.parseBoolean(value.stringValue()));
                }
            }
        }
    }
}
