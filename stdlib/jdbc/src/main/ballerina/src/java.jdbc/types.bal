// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/config;

# Provides a set of configurations for the JDBC Client.
#
# + url - URL of the database to connect
# + username - Username for the database connection
# + password - Password for the database connection
# + poolOptions - Properties for the connection pool configuration. Refer `PoolOptions` for more details
# + dbOptions - A map of DB specific properties. These properties will have an effect only if the dataSourceClassName is
#               provided in poolOptions
public type ClientConfiguration record {|
    string url = "";
    string username = "";
    string password = "";
    PoolOptions poolOptions?;
    map<anydata> dbOptions = {};
|};

# Represents the properties which are used to configure DB connection pool.
# Default values of the fields can be set through the configuration API.
#
# + connectionInitSql - SQL statement that will be executed after every new connection creation before adding it
#                       to the pool. Default value is none and it can be changed through the
#                       configuration API with the key `b7a.jdbc.pool.connectionInitSql`.
# + dataSourceClassName - Name of the DataSource class provided by the JDBC driver. This is used on following scenarios.
#                         1. In JDBC client when DB specific properties are required (which are given with dbOptions)
#                         2. In any data client in which XA transactions enabled by isXA property and need to provide a
#                         custom XA implementation.Default value is none and it can be changed through
#                         the configuration API with the key `b7a.jdbc.pool.dataSourceClassName`.
# + autoCommit - Auto-commit behavior of connections returned from the pool.
#                Default value is `true` and it can be changed through the configuration API with the key
#                `b7a.jdbc.pool.autoCommit`.
# + isXA - Whether connections are used for a distributed transaction. Default value is `false` and it
#          can be set through the configuration API with the key `b7a.jdbc.pool.isXA`.
# + maximumPoolSize - The maximum size that the pool is allowed to reach, including both idle and in-use connections.
#                     Default value is 15 and it can be changed through the configuration API with the key
#                     `b7a.jdbc.pool.maximumPoolSize`.
# + connectionTimeoutInMillis - The maximum number of milliseconds that a client will wait for a connection from the
#                       pool. Default value is 30000 (30 seconds) and it can be changed through the configuration API
#                       with the key `b7a.jdbc.pool.connectionTimeoutInMillis`. Lowest acceptable connection timeout
#                       value is 250 ms.
# + idleTimeoutInMillis - The maximum amount of time that a connection is allowed to sit idle in the pool. Default value
#                 is 600000 (10 minutes) and it can be changed through the configuration API with the key
#                 `b7a.jdbc.pool.idleTimeoutInMillis`. The minimum allowed value is 10000ms (10 seconds).
#                 This setting only applies when minimumIdle is defined to be less than maximumPoolSize.
# + minimumIdle - The minimum number of idle connections that pool tries to maintain in the pool. Default is the same as
#                 maximumPoolSize and it can be changed through the configuration API with the key
#                 `b7a.jdbc.pool.minimumIdle`.
# + maxLifetimeInMillis - The maximum lifetime of a connection in the pool. Default value is 1800000 (30 minutes)
#                  and it can be changed through the configuration API with the key `b7a.jdbc.pool.maxLifetimeInMillis`.
#                  A value of 0 indicates unlimited maximum lifetime (infinite lifetime), subject to
#                  the `idleTimeoutInMillis`.
# + validationTimeoutInMillis - The maximum duration of time that a connection will be tested for aliveness. Default
#                       value is 5000 (5 seconds) and it can be changed through the configuration API with the key
#                       `b7a.jdbc.pool.validationTimeoutInMillis`.  Lowest acceptable validation timeout is 250 ms.
public type PoolOptions record {|
    string connectionInitSql = config:getAsString("b7a.jdbc.pool.connectionInitSql", "");
    string dataSourceClassName = config:getAsString("b7a.jdbc.pool.dataSourceClassName", "");
    boolean autoCommit = config:getAsBoolean("b7a.jdbc.pool.autoCommit", true);
    boolean isXA = config:getAsBoolean("b7a.jdbc.pool.isXA", false);
    int maximumPoolSize = config:getAsInt("\"b7a.jdbc.pool.maximumPoolSize\"", 15);
    int connectionTimeoutInMillis = config:getAsInt("\"b7a.jdbc.pool.connectionTimeoutInMillis\"", 30000);
    int idleTimeoutInMillis = config:getAsInt("b7a.jdbc.pool.idleTimeoutInMillis", 600000);
    int minimumIdle = config:getAsInt("b7a.jdbc.pool.minimumIdle", 15);
    int maxLifetimeInMillis = config:getAsInt("b7a.jdbc.pool.maxLifetimeInMillis", 1800000);
    int validationTimeoutInMillis = config:getAsInt("\"b7a.jdbc.pool.validationTimeoutInMillis\"", 5000);
|};

// This is a container object that holds the global pool config and initilizes the internal map of connection pools
type GlobalPoolConfigContainer object {
    private PoolOptions poolConfig = {};

    function __init() {
        // poolConfig record is frozen so that it cannot be modified during runtime
        PoolOptions frozenConfig = self.poolConfig.cloneReadOnly();
        self.initGlobalPoolContainer(frozenConfig);
    }

    function initGlobalPoolContainer(PoolOptions poolConfig) = external;

    function getGlobalPoolConfig() returns PoolOptions {
        return self.poolConfig;
    }
};

// This is an instance of GlobalPoolConfigContainer object type. The __init functions of database clients pass
// poolConfig member of this instance to the external client creation logic in order to access the internal map
// of connection pools.
final GlobalPoolConfigContainer globalPoolConfigContainer = new;

# The SQL Datatype of the parameter.
#
# `VARCHAR` - Small, variable-length character string
# `CHAR` - Small, fixed-length character string
# `LONGVARCHAR` - Large, variable-length character string
# `NCHAR` - Small, fixed-length character string with unicode support
# `LONGNVARCHAR` - Large, variable-length character string with Unicode support
#
# `BIT` - Single bit value that can be zero or one, or nil
# `BOOLEAN` - Boolean value either True or false
# `TINYINT` - 8-bit integer value which may be unsigned or signed
# `SMALLINT` - 16-bit signed integer value which may be unsigned or signed
# `INTEGER` - 32-bit signed integer value which may be unsigned or signed
# `BIGINT` - 64-bit signed integer value which may be unsigned or signed
#
# `NUMERIC` - Fixed-precision and scaled decimal values
# `DECIMAL` - Fixed-precision and scaled decimal values
# `REAL` - Single precision floating point number
# `FLOAT` - Double precision floating point number
# `DOUBLE` - Double precision floating point number
#
# `BINARY` - Small, fixed-length binary value
# `BLOB` - Binary Large Object
# `LONGVARBINARY` - Large, variable-length binary value
# `VARBINARY` - Small, variable-length binary value
#
# `CLOB` - Character Large Object.
# `NCLOB` - Character large objects in multibyte national character set
#
# `DATE` - Date consisting of day, month, and year
# `TIME` - Time consisting of hours, minutes, and seconds
# `DATETIME` - Both DATE and TIME with additional a nanosecond field
# `TIMESTAMP` - Both DATE and TIME with additional a nanosecond field
#
# `ARRAY` - Composite data value that consists of zero or more elements of a specified data type
# `STRUCT` - User-defined structured type, consists of one or more attributes
# `REFCURSOR` - Cursor value
public type SQLType TYPE_VARCHAR|TYPE_CHAR|TYPE_LONGVARCHAR|TYPE_NCHAR|TYPE_LONGNVARCHAR|TYPE_NVARCHAR|TYPE_BIT|
TYPE_BOOLEAN|TYPE_TINYINT|TYPE_SMALLINT|TYPE_INTEGER|TYPE_BIGINT|TYPE_NUMERIC|TYPE_DECIMAL|TYPE_REAL|TYPE_FLOAT|
TYPE_DOUBLE|TYPE_BINARY|TYPE_BLOB|TYPE_LONGVARBINARY|TYPE_VARBINARY|TYPE_CLOB|TYPE_NCLOB|TYPE_DATE|TYPE_TIME|
TYPE_DATETIME|TYPE_TIMESTAMP|TYPE_ARRAY|TYPE_STRUCT|TYPE_REFCURSOR;

public const TYPE_VARCHAR = "VARCHAR";
public const TYPE_CHAR = "CHAR";
public const TYPE_LONGVARCHAR = "LONGVARCHAR";
public const TYPE_NCHAR = "NCHAR";
public const TYPE_LONGNVARCHAR = "LONGNVARCHAR";
public const TYPE_NVARCHAR = "NVARCHAR";
public const TYPE_BIT = "BIT";
public const TYPE_BOOLEAN = "BOOLEAN";
public const TYPE_TINYINT = "TINYINT";
public const TYPE_SMALLINT = "SMALLINT";
public const TYPE_INTEGER = "INTEGER";
public const TYPE_BIGINT = "BIGINT";
public const TYPE_NUMERIC = "NUMERIC";
public const TYPE_DECIMAL = "DECIMAL";
public const TYPE_REAL = "REAL";
public const TYPE_FLOAT = "FLOAT";
public const TYPE_DOUBLE = "DOUBLE";
public const TYPE_BINARY = "BINARY";
public const TYPE_BLOB = "BLOB";
public const TYPE_LONGVARBINARY = "LONGVARBINARY";
public const TYPE_VARBINARY = "VARBINARY";
public const TYPE_CLOB = "CLOB";
public const TYPE_NCLOB = "NCLOB";
public const TYPE_DATE = "DATE";
public const TYPE_TIME = "TIME";
public const TYPE_DATETIME = "DATETIME";
public const TYPE_TIMESTAMP = "TIMESTAMP";
public const TYPE_ARRAY = "ARRAY";
public const TYPE_STRUCT = "STRUCT";
public const TYPE_REFCURSOR = "REFCURSOR";

# The direction of the parameter.
#
# `IN` - IN parameters are used to send values to stored procedures
# `OUT` - OUT parameters are used to get values from stored procedures
# `INOUT` - INOUT parameters are used to send values and get values from stored procedures
public type Direction DIRECTION_IN|DIRECTION_OUT|DIRECTION_INOUT;

public const DIRECTION_IN = "IN";
public const DIRECTION_OUT = "OUT";
public const DIRECTION_INOUT = "INOUT";

# Represents a parameter for the JDBC Client remote functions when a variable needs to be passed
# to the remote function.
#
# + sqlType - The data type of the corresponding SQL parameter
# + value - Value of paramter passed into the SQL statement
# + direction - Direction of the SQL Parameter IN, OUT, or INOUT - Default value is IN
# + recordType - In case of OUT direction, if the sqlType is REFCURSOR, this represents the record type to map a
#                result row
public type Parameter record {|
    SQLType sqlType;
    anydata value = ();
    Direction direction = DIRECTION_IN;
    typedesc<record {}> recordType?;
|};

# Represents the output of the `update` remote function.
#
# + updatedRowCount - The updated row count during the sql statement exectuion
# + generatedKeys - A map of auto generated key values during the sql statement execution
public type UpdateResult record {|
    int updatedRowCount;
    map<anydata> generatedKeys;
|};

# Represents the output of the `batchUpdate` remote function.
#
# + updatedRowCount - The updated row count during the sql statement exectuion
#            A number greater than or equal to zero - indicates that the command was processed successfully
#                                                     and is an update count giving the number of rows
#            A value of -2 - Indicates that the command was processed successfully but that the number of rows
#                            affected is unknown
#            A value of -3 - Indicates that the command failed to execute successfully and occurs only if a driver
#                            continues to process remaining commands after a command fails
# + generatedKeys - A map of auto generated key values during the batch update execution
# + returnedError - The `Error` returned from the remote function in case of a failure
public type BatchUpdateResult record {|
    int[] updatedRowCount;
    map<anydata[]> generatedKeys;
    Error? returnedError;
|};

# The parameter passed into the operations.
public type Param string|int|boolean|float|decimal|byte[]|Parameter;
