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

# Represents the properties which are used to configure DB connection pool.
#
# + connectionInitSql - SQL statement that will be executed after every new connection creation before adding it
#                       to the pool
# + dataSourceClassName - Name of the DataSource class provided by the JDBC driver. This is used on following scenarios.
#                         1. In JDBC client when DB specific properties are required (which are given with dbOptions)
#                         2. In any data client in which XA transactions enabled by isXA property and need to provide a custom XA implementation.
# + autoCommit - Auto-commit behavior of connections returned from the pool
# + isXA - Whether Connections are used for a distributed transaction
# + maximumPoolSize - Maximum size that the pool is allowed to reach, including both idle and in-use connections
# + connectionTimeout - Maximum number of milliseconds that a client will wait for a connection from the pool. Default is 30 seconds
# + idleTimeout - Maximum amount of time that a connection is allowed to sit idle in the pool. Default is 10 minutes
# + minimumIdle - Minimum number of idle connections that pool tries to maintain in the pool. Default is same as maximumPoolSize
# + maxLifetime - Maximum lifetime of a connection in the pool. Default is 30 minutes
# + validationTimeout - Maximum amount of time that a connection will be tested for aliveness. Default 5 seconds
public type PoolOptions record {
    string connectionInitSql = "";
    string dataSourceClassName = "";
    boolean autoCommit = true;
    boolean isXA = false;
    int maximumPoolSize = 10;
    int connectionTimeout = 30000;
    int idleTimeout = 600000;
    int minimumIdle = -1;
    int maxLifetime = 1800000;
    int validationTimeout = 5000;
    !...
};

# The SQL Datatype of the parameter.
#
# VARCHAR - Small, variable length character string
# CHAR - Small, fixed length character string
# LONGVARCHAR - Large, variable length character string
# NCHAR - Small, fixed length character string with unicode support
# LONGNVARCHAR - Large, variable length character string with unicode support
#
# BIT - Single bit value that can be zero or one, or nil
# BOOLEAN - Boolean value either True or false
# TINYINT - 8-bit integer value which may be unsigned or signed
# SMALLINT - 16-bit signed integer value which may be unsigned or signed
# INTEGER - 32-bit signed integer value which may be unsigned or signed
# BIGINT - 64-bit signed integer value which may be unsigned or signed
#
# NUMERIC - Fixed-precision and scaled decimal values
# DECIMAL - Fixed-precision and scaled decimal values
# REAL - Single precision floating point number
# FLOAT - Double precision floating point number
# DOUBLE - Double precision floating point number
#
# BINARY - Small, fixed-length binary value
# BLOB - Binary Large Object
# LONGVARBINARY - Large, variable length binary value
# VARBINARY - Small, variable length binary value
#
# CLOB - Character Large Object.
# NCLOB - Character large objects in multibyte national character set
#
# DATE - Date consisting of day, month, and year
# TIME - Time consisting of hours, minutes, and seconds
# DATETIME - Both DATE and TIME with additional a nanosecond field
# TIMESTAMP - Both DATE and TIME with additional a nanosecond field
#
# ARRAY - Composite data value that consists of zero or more elements of a specified data type
# STRUCT - User defined structured type, consists of one or more attributes
# REFCURSOR - Cursor value
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
# IN - IN parameters are used to send values to stored procedures
# OUT - OUT parameters are used to get values from stored procedures
# INOUT - INOUT parameters are used to send values and get values from stored procedures
public type Direction DIRECTION_IN|DIRECTION_OUT|DIRECTION_INOUT;

public const DIRECTION_IN = "IN";
public const DIRECTION_OUT = "OUT";
public const DIRECTION_INOUT = "INOUT";

# Parameter represents a parameter for the SQL actions when a variable parameter needs to be passed to the action.
#
# + sqlType - The data type of the corresponding SQL parameter
# + value - Value of paramter passed into the SQL statement
# + direction - Direction of the SQL Parameter IN, OUT, or INOUT - Default value is IN
# + recordType - In case of OUT direction, if the sqlType is REFCURSOR, this represents the record type to map a
#                result row
public type Parameter record {
    SQLType sqlType;
    any value = ();
    Direction direction = DIRECTION_IN;
    typedesc recordType?;
    !...
};

# The parameter passed into the operations.
type Param string|int|boolean|float|byte[]|Parameter;

public type DatabaseErrorData record {
    string message;
    !...
};
