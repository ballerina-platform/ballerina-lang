/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerina.core.nativeimpl.connectors.data.sql;

import com.zaxxer.hikari.HikariDataSource;
import org.osgi.service.component.annotations.Component;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BMap;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaConnector;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeConnector;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Native RDBMS Connector.
 */
@BallerinaConnector(
        packageName = "ballerina.data.sql",
        connectorName = SQLConnector.CONNECTOR_NAME,
        args = {
                @Argument(name = "options",
                          type = TypeEnum.MAP)
        })
@Component(
        name = "ballerina.data.connectors.sql",
        immediate = true,
        service = AbstractNativeConnector.class)
public class SQLConnector extends AbstractNativeConnector {

    public static final String CONNECTOR_NAME = "Connector";

    private HikariDataSource hikariDataSource;

    @Override
    public boolean init(BValue[] bValueRefs) {
        if (bValueRefs != null && bValueRefs.length == 1) {
            BMap options = (BMap) bValueRefs[0];
            buildDataSource(options);
        }
        return true;
    }

    @Override
    public AbstractNativeConnector getInstance() {
        return new SQLConnector();
    }

    @Override
    public String getPackageName() {
        return "ballerina.data.sql";
    }

    public Connection getSQLConnection() {
        Connection conn = null;
        if (hikariDataSource != null) {
            try {
                conn = hikariDataSource.getConnection();
            } catch (SQLException e) {
                throw new BallerinaException(
                        "Error in creating Connection in " + SQLConnector.CONNECTOR_NAME + ". " + e.getMessage(), e);
            }
        }
        return conn;
    }

    private void buildDataSource(BMap options) {
        hikariDataSource = new HikariDataSource();
        BValue value = options.get(new BString(Constants.DATA_SOURCE_CLASSNAME));
        if (value != null) {
            hikariDataSource.setDataSourceClassName(value.stringValue());
        }
        value = options.get(new BString(Constants.JDBC_URL));
        if (value != null) {
            hikariDataSource.setJdbcUrl(value.stringValue());
        }
        value = options.get(new BString(Constants.USER_NAME));
        if (value != null) {
            hikariDataSource.setUsername(value.stringValue());
        }
        value = options.get(new BString(Constants.PASSWORD));
        if (value != null) {
            hikariDataSource.setPassword(value.stringValue());
        }
        value = options.get(new BString(Constants.AUTO_COMMIT));
        if (value != null) {
            hikariDataSource.setAutoCommit(Boolean.parseBoolean(value.stringValue()));
        }
        value = options.get(new BString(Constants.CONNECTION_TIMEOUT));
        if (value != null) {
            hikariDataSource.setConnectionTimeout(Long.parseLong(value.stringValue()));
        }
        value = options.get(new BString(Constants.IDLE_TIMEOUT));
        if (value != null) {
            hikariDataSource.setIdleTimeout(Long.parseLong(value.stringValue()));
        }
        value = options.get(new BString(Constants.MAX_LIFETIME));
        if (value != null) {
            hikariDataSource.setMaxLifetime(Long.parseLong(value.stringValue()));
        }
        value = options.get(new BString(Constants.CONNECTION_TEST_QUERY));
        if (value != null) {
            hikariDataSource.setConnectionTestQuery(value.stringValue());
        }
        value = options.get(new BString(Constants.MINIMUM_IDLE));
        if (value != null) {
            hikariDataSource.setMinimumIdle(Integer.parseInt(value.stringValue()));
        }
        value = options.get(new BString(Constants.MAXIMUM_POOL_SIZE));
        if (value != null) {
            hikariDataSource.setMaximumPoolSize(Integer.parseInt(value.stringValue()));
        }
        value = options.get(new BString(Constants.POOOL_NAME));
        if (value != null) {
            hikariDataSource.setPoolName(value.stringValue());
        }
        value = options.get(new BString(Constants.ISOLATE_INTERNAL_QUERIES));
        if (value != null) {
            hikariDataSource.setIsolateInternalQueries(Boolean.parseBoolean(value.stringValue()));
        }
        value = options.get(new BString(Constants.ALLOW_POOL_SUSPENSION));
        if (value != null) {
            hikariDataSource.setAllowPoolSuspension(Boolean.parseBoolean(value.stringValue()));
        }
        value = options.get(new BString(Constants.READ_ONLY));
        if (value != null) {
            hikariDataSource.setReadOnly(Boolean.parseBoolean(value.stringValue()));
        }
        value = options.get(new BString(Constants.REGISTER_MBEANS));
        if (value != null) {
            hikariDataSource.setRegisterMbeans(Boolean.parseBoolean(value.stringValue()));
        }
        value = options.get(new BString(Constants.CATALOG));
        if (value != null) {
            hikariDataSource.setCatalog(value.stringValue());
        }
        value = options.get(new BString(Constants.CONNECTION_INIT_SQL));
        if (value != null) {
            hikariDataSource.setConnectionInitSql(value.stringValue());
        }
        value = options.get(new BString(Constants.DRIVER_CLASSNAME));
        if (value != null) {
            hikariDataSource.setDriverClassName(value.stringValue());
        }
        value = options.get(new BString(Constants.TRANSACTION_ISOLATION));
        if (value != null) {
            hikariDataSource.setTransactionIsolation(value.stringValue());
        }
        value = options.get(new BString(Constants.VALIDATION_TIMEOUT));
        if (value != null) {
            hikariDataSource.setValidationTimeout(Long.parseLong(value.stringValue()));
        }
        value = options.get(new BString(Constants.LEAK_DETECTION_THRESHOLD));
        if (value != null) {
            hikariDataSource.setLeakDetectionThreshold(Long.parseLong(value.stringValue()));
        }
    }
}
