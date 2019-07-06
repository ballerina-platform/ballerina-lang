/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This wraps a BMap instance representing PoolOptions Ballerina record type.
 * PoolOptions type encapsulates a map of shared SQLDatasources against JDBC URL.
 * This class provides methods to manipulate aforementioned map.
 */
public class PoolOptionsWrapper {
    private final MapValue<String, Object> poolOptions;

    protected PoolOptionsWrapper(MapValue<String, Object> poolOptions) {
        this.poolOptions = poolOptions;
    }

    /**
     * Retrieve the {@link SQLDatasource}} object corresponding to the provided JDBC URL in
     * {@link org.ballerinalang.database.sql.SQLDatasource.SQLDatasourceParams}.
     * Creates a datasource if it doesn't exist.
     *
     * @param sqlDatasourceParams datasource parameters required to retrieve the JDBC URL for datasource lookup and
     *                            initialization of the newly created datasource if it doesn't exists
     * @return The existing or newly created {@link SQLDatasource} object
     */
    public SQLDatasource retrieveDatasource(SQLDatasource.SQLDatasourceParams sqlDatasourceParams) {
        ConcurrentMap<String, SQLDatasource> hikariDatasourceMap = createPoolMapIfNotExists();
        SQLDatasource existingSqlDatasource = hikariDatasourceMap.get(sqlDatasourceParams.getJdbcUrl());
        SQLDatasource sqlDatasourceToBeReturned = existingSqlDatasource;
        if (existingSqlDatasource != null) {
            acquireDatasourceMutex(existingSqlDatasource);
            try {
                if (!existingSqlDatasource.isPoolShutdown()) {
                    existingSqlDatasource.incrementClientCounter();
                } else {
                    sqlDatasourceToBeReturned = hikariDatasourceMap.compute(sqlDatasourceParams.getJdbcUrl(),
                            (key, value) -> createAndInitDatasource(sqlDatasourceParams));
                }
            } finally {
                existingSqlDatasource.releaseMutex();
            }
        } else {
            sqlDatasourceToBeReturned = hikariDatasourceMap.computeIfAbsent(sqlDatasourceParams.getJdbcUrl(),
                    key -> createAndInitDatasource(sqlDatasourceParams));

        }
        return sqlDatasourceToBeReturned;
    }

    private void acquireDatasourceMutex(SQLDatasource sqlDatasource) {
        try {
            sqlDatasource.acquireMutex();
        } catch (InterruptedException e) {
            throw new BallerinaException("error in obtaining a connection pool");
        }
    }

    private SQLDatasource createAndInitDatasource(SQLDatasource.SQLDatasourceParams sqlDatasourceParams) {
        SQLDatasource newSqlDatasource = new SQLDatasource().init(sqlDatasourceParams);
        newSqlDatasource.incrementClientCounter();
        return newSqlDatasource;
    }

    /**
     * Retrieve the value of the field corresponding to the provided field.
     *
     * @param fieldName The name of the field to retrieve the value of
     * @return The value of the field
     */
    public Object get(String fieldName) {
        return poolOptions.get(fieldName);
    }

    /**
     * Retrieve the value of the field corresponding to the provided field.
     *
     * @param fieldName The name of the field to retrieve the value of
     * @return The value of the field
     */
    public Long getInt(String fieldName) {
        return poolOptions.getIntValue(fieldName);
    }

    /**
     * Retrieve the value of the field corresponding to the provided field.
     *
     * @param fieldName The name of the field to retrieve the value of
     * @return The value of the field
     */
    public String getString(String fieldName) {
        return poolOptions.getStringValue(fieldName);
    }

    /**
     * Retrieve the value of the field corresponding to the provided field.
     *
     * @param fieldName The name of the field to retrieve the value of
     * @return The value of the field
     */
    public Boolean getBoolean(String fieldName) {
        return poolOptions.getBooleanValue(fieldName);
    }

    private ConcurrentMap<String, SQLDatasource> createPoolMapIfNotExists() {
        ConcurrentHashMap<String, SQLDatasource> hikariDatasourceMap = SQLDatasourceUtils
                .retrieveDatasourceContainer(poolOptions);
        // map could be null only in a local pool creation scenario
        if (hikariDatasourceMap == null) {
            synchronized (this.poolOptions) {
                hikariDatasourceMap = SQLDatasourceUtils.retrieveDatasourceContainer(poolOptions);
                if (hikariDatasourceMap == null) {
                    hikariDatasourceMap = new ConcurrentHashMap<>();
                    SQLDatasourceUtils.addDatasourceContainer(poolOptions, hikariDatasourceMap);
                }
            }
        }
        return hikariDatasourceMap;
    }
}
