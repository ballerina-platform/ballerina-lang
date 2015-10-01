/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.extension.eventtable.rdbms;

import org.apache.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.wso2.siddhi.query.api.annotation.Element;

import java.util.List;

public class PooledDataSource {

    private static final int maxIdle = 8;
    private static final int minIdle = 0;
    private static final int initialSize = 0;
    private static final int maxActive = 50;
    private static final int maxWait = 60000;
    private static final boolean testOnBorrow = true;
    private static final String validationQuery = "SELECT 1";
    private static final long validationInterval = 30000;
    private static final boolean defaultAutoCommit = false;
    private static final Logger log = Logger.getLogger(PooledDataSource.class);


    private PooledDataSource() {
    }

    public static DataSource getPoolDataSource(String driver, String url, String username, String password, List<Element> connectionPropertyElements) {
        DataSource dataSource = new DataSource();
        PoolProperties poolProperties = new PoolProperties();
        poolProperties.setDriverClassName(driver);
        poolProperties.setUrl(url);
        poolProperties.setUsername(username);
        poolProperties.setPassword(password);
        poolProperties.setMaxIdle(maxIdle);
        poolProperties.setMinIdle(minIdle);
        poolProperties.setInitialSize(initialSize);
        poolProperties.setMaxActive(maxActive);
        poolProperties.setMaxWait(maxWait);
        poolProperties.setTestOnBorrow(testOnBorrow);
        poolProperties.setValidationQuery(validationQuery);
        poolProperties.setValidationInterval(validationInterval);
        poolProperties.setDefaultAutoCommit(defaultAutoCommit);

        if (connectionPropertyElements != null) {
            for (Element element : connectionPropertyElements) {
                String elementKey = element.getKey();
                try {
                    if ("maxIdle".equalsIgnoreCase(elementKey)) {
                        int value = Integer.parseInt(element.getValue());
                        poolProperties.setMaxIdle(value);
                    } else if ("minIdle".equalsIgnoreCase(elementKey)) {
                        int value = Integer.parseInt(element.getValue());
                        poolProperties.setMinIdle(value);
                    } else if ("initialSize".equalsIgnoreCase(elementKey)) {
                        int value = Integer.parseInt(element.getValue());
                        poolProperties.setInitialSize(value);
                    } else if ("maxActive".equalsIgnoreCase(elementKey)) {
                        int value = Integer.parseInt(element.getValue());
                        poolProperties.setMaxActive(value);
                    } else if ("maxWait".equalsIgnoreCase(elementKey)) {
                        int value = Integer.parseInt(element.getValue());
                        poolProperties.setMaxWait(value);
                    } else if ("testOnBorrow".equalsIgnoreCase(elementKey)) {
                        boolean value = Boolean.parseBoolean(element.getValue());
                        poolProperties.setTestOnBorrow(value);
                    } else if ("validationQuery".equalsIgnoreCase(elementKey)) {
                        poolProperties.setValidationQuery(element.getValue());
                    } else if ("validationInterval".equalsIgnoreCase(elementKey)) {
                        long value = Long.parseLong(element.getValue());
                        poolProperties.setValidationInterval(value);
                    } else if ("testOnReturn".equalsIgnoreCase(elementKey)) {
                        boolean value = Boolean.parseBoolean(element.getValue());
                        poolProperties.setTestOnReturn(value);
                    } else if ("testWhileIdle".equalsIgnoreCase(elementKey)) {
                        boolean value = Boolean.parseBoolean(element.getValue());
                        poolProperties.setTestWhileIdle(value);
                    } else if ("validatorClassName".equalsIgnoreCase(elementKey)) {
                        poolProperties.setValidatorClassName(element.getValue());
                    } else if ("timeBetweenEvictionRunsMillis".equalsIgnoreCase(elementKey)) {
                        int value = Integer.parseInt(element.getValue());
                        poolProperties.setTimeBetweenEvictionRunsMillis(value);
                    } else if ("numTestsPerEvictionRun".equalsIgnoreCase(elementKey)) {
                        int value = Integer.parseInt(element.getValue());
                        poolProperties.setNumTestsPerEvictionRun(value);
                    } else if ("minEvictableIdleTimeMillis".equalsIgnoreCase(elementKey)) {
                        int value = Integer.parseInt(element.getValue());
                        poolProperties.setMinEvictableIdleTimeMillis(value);
                    } else if ("accessToUnderlyingConnectionAllowed".equalsIgnoreCase(elementKey)) {
                        boolean value = Boolean.parseBoolean(element.getValue());
                        poolProperties.setAccessToUnderlyingConnectionAllowed(value);
                    } else if ("removeAbandoned".equalsIgnoreCase(elementKey)) {
                        boolean value = Boolean.parseBoolean(element.getValue());
                        poolProperties.setRemoveAbandoned(value);
                    } else if ("removeAbandonedTimeout".equalsIgnoreCase(elementKey)) {
                        int value = Integer.parseInt(element.getValue());
                        poolProperties.setRemoveAbandonedTimeout(value);
                    } else if ("logAbandoned".equalsIgnoreCase(elementKey)) {
                        boolean value = Boolean.parseBoolean(element.getValue());
                        poolProperties.setLogAbandoned(value);
                    } else if ("initSQL".equalsIgnoreCase(elementKey)) {
                        poolProperties.setInitSQL(element.getValue());
                    } else if ("jdbcInterceptors".equalsIgnoreCase(elementKey)) {
                        poolProperties.setJdbcInterceptors(element.getValue());
                    } else if ("jmxEnabled".equalsIgnoreCase(elementKey)) {
                        boolean value = Boolean.parseBoolean(element.getValue());
                        poolProperties.setJmxEnabled(value);
                    } else if ("fairQueue".equalsIgnoreCase(elementKey)) {
                        boolean value = Boolean.parseBoolean(element.getValue());
                        poolProperties.setFairQueue(value);
                    } else if ("abandonWhenPercentageFull".equalsIgnoreCase(elementKey)) {
                        int value = Integer.parseInt(element.getValue());
                        poolProperties.setAbandonWhenPercentageFull(value);
                    } else if ("maxAge".equalsIgnoreCase(elementKey)) {
                        long value = Long.parseLong(element.getValue());
                        poolProperties.setMaxAge(value);
                    } else if ("useEquals".equalsIgnoreCase(elementKey)) {
                        boolean value = Boolean.parseBoolean(element.getValue());
                        poolProperties.setUseEquals(value);
                    } else if ("suspectTimeout".equalsIgnoreCase(elementKey)) {
                        int value = Integer.parseInt(element.getValue());
                        poolProperties.setSuspectTimeout(value);
                    } else if ("validationQueryTimeout".equalsIgnoreCase(elementKey)) {
                        int value = Integer.parseInt(element.getValue());
                        poolProperties.setValidationQueryTimeout(value);
                    } else if ("alternateUsernameAllowed".equalsIgnoreCase(elementKey)) {
                        boolean value = Boolean.parseBoolean(element.getValue());
                        poolProperties.setAlternateUsernameAllowed(value);
                    }
                } catch (NumberFormatException e) {
                    log.error("Invalid database connection property value: " + element.getValue() + ", "
                            + "ignoring property " + elementKey);
                }
            }
        }

        dataSource.setPoolProperties(poolProperties);
        return dataSource;
    }


}
