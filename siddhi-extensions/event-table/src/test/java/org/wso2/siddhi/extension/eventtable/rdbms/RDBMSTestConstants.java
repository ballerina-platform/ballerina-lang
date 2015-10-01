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

public interface RDBMSTestConstants {

    public static final String MYSQL_DRIVER_CLASS = "com.mysql.jdbc.Driver";
    public static final String CONNECTION_URL = "jdbc:mysql://localhost:3306/cepdb";
    public static final String H2_CONNECTION_URL = "jdbc:h2:~/cepdb";
    public static final String ORACLE_CONNECTION_URL = "jdbc:oracle:thin:@192.168.18.8:1521/daniddb";
    public static final String H2_DRIVER_CLASS = "org.h2.Driver";
    public static final String ORACLE_DRIVER_CLASS = "oracle.jdbc.OracleDriver";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "root";
    public static final String TABLE_NAME = "table1";
    public static String DATA_SOURCE_NAME = "cepDataSource";
    public static final String H2USERNAME = "wso2carbon";
    public static final String ORACLEUSERNAME = "Mohan100";
    public static final String H2PASSWORD = "wso2carbon";
    public static final String ORACLEPASSWORD = "Mohan100";
}
