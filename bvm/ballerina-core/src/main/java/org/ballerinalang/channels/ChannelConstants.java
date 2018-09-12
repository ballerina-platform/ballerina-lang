/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.channels;

/**
 * Constants related to channel implementation.
 * @since 0.982.0
 */
public class ChannelConstants {

    public static final String DB_NAME = "channels";
    private static final String TABLE_NAME = "messages";
    public static final String DB_USERNAME = "SA";
    public static final String DB_PASSWORD = "";
    public static final String CONF_NAMESPACE = "b7a.channel.db.";
    public static final String CONF_USERNAME = "username";
    public static final String CONF_PASSWORD = "password";
    public static final String CONF_DB_TYPE = "dbType";
    public static final String CONF_HOST_OR_PATH = "hostOrPath";
    public static final String CONF_PORT = "port";
    public static final String CONF_DB_NAME = "dbName";
    public static final String CONF_DB_OPTIONS = "dbOptions";

    /**
     * DB queries.
     */
    public static final String CREATE = "create table IF NOT EXISTS " + TABLE_NAME + " (msgId int NOT NULL " +
            "AUTO_INCREMENT,channelName varchar(200),msgKey varchar(4000),value CLOB, constraint pk primary " +
            "key ( msgId ))";
    public static final String SELECT = "SELECT msgId,value FROM " + TABLE_NAME + " WHERE channelName = ? AND msgKey " +
            "= ?";
    public static final String SELECT_NULL = "SELECT msgId,value FROM " + TABLE_NAME + " WHERE channelName = ? AND " +
            "msgKey IS NULL";
    public static final String INSERT = "INSERT into " + TABLE_NAME + " (channelName, msgKey, value) values (?, ?, ?)";
    public static final String DROP = "DELETE FROM " + TABLE_NAME + " where msgId = ?";
    /**
     * DB Types with first class support.
     */
    public static final class DBTypes {
        public static final String SQLSERVER = "SQLSERVER";
        public static final String ORACLE = "ORACLE";
        public static final String SYBASE = "SYBASE";
        public static final String POSTGRESQL = "POSTGRESQL";
        public static final String IBMDB2 = "DB2";

        public static final String DERBY_SERVER = "DERBY_SERVER";
        public static final String DERBY_FILE = "DERBY_FILE";

        public static final String HSQLDB_SERVER = "HSQLDB_SERVER";
        public static final String HSQLDB_FILE = "HSQLDB_FILE";

        public static final String MYSQL = "MYSQL";

        public static final String H2 = "H2";
        public static final String H2_SERVER = "H2_SERVER";
        public static final String H2_FILE = "H2_FILE";
        public static final String H2_MEMORY = "H2_MEMORY";
    }

    /**
     * Constants default DB ports.
     */
    public static final class DefaultPort {
        public static final int MYSQL = 3306;
        public static final int SQLSERVER = 1433;
        public static final int ORACLE = 1521;
        public static final int SYBASE = 5000;
        public static final int POSTGRES = 5432;
        public static final int IBMDB2 = 50000;
        public static final int HSQLDB_SERVER = 9001;
        public static final int H2_SERVER = 9092;
        public static final int DERBY_SERVER = 1527;
    }
}
