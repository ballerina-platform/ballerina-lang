import ballerina/config;
import ballerina/time;

# Represents the properties which are used to configure DB connection pool.
# Default values of the fields can be set through the configuration API.
#
# + maxOpenConnections - The maximum number of open connections that the pool is allowed to have, including both idle and in-use connections.
#                     Default value is 15 and it can be changed through the configuration API with the key
#                     `b7a.sql.pool.maximumPoolSize`.
# + minIdleConnections - The minimum number of idle connections that pool tries to maintain in the pool. Default is the same as
#                 maxOpenConnections and it can be changed through the configuration API with the key
#                 `b7a.sql.pool.minimumIdle`.
# + maxConnectionLifeTimeSeconds - The maximum lifetime of a connection in the pool. Default value is 1800 seconds (30 minutes)
#                  and it can be changed through the configuration API with the key `b7a.sql.pool.maxLifetimeInMillis`.
#                  A value of 0 indicates unlimited maximum lifetime (infinite lifetime).
public type ConnectionPoolOptions record {|
    //TODO: Is this name should be changed to ConnectionPool?
    //whether it's only placeholer for config? or actual underneath object is also stored?
    int maxOpenConnections = config:getAsInt("\"b7a.sql.pool.maxOpenConnections\"", 15);
    decimal maxConnectionLifeTimeSeconds = <decimal>config:getAsFloat("b7a.sql.pool.maxConnectionLifeTime", 1800.0);
    int minIdleConnections = config:getAsInt("b7a.sql.pool.minIdleConnections", 15);
|};

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
public type SQLType
    [TYPE_VARCHAR, string] |
    [TYPE_CHAR, string] |
    [TYPE_LONGVARCHAR, string] |
    [TYPE_NCHAR, string] |
    [TYPE_LONGNVARCHAR, string] |
    [TYPE_NVARCHAR, string ] |
    [TYPE_BIT, boolean] |
    [TYPE_BOOLEAN, boolean] |
    [TYPE_TINYINT, int] |
    [TYPE_SMALLINT, int] |
    [TYPE_INTEGER,  int] |
    [TYPE_BIGINT, int] |
    [TYPE_NUMERIC, decimal] |
    [TYPE_DECIMAL, decimal] |
    [TYPE_REAL, decimal] |
    [TYPE_FLOAT, float] |
    [TYPE_DOUBLE, float] |
    [TYPE_BINARY, byte[]] |
    [TYPE_BLOB, byte[]] |
    [TYPE_LONGVARBINARY, byte[]] |
    [TYPE_VARBINARY, byte[]] |
    [TYPE_CLOB, string] |
    [TYPE_NCLOB, string] |
    [TYPE_DATE, time:Time] |
    [TYPE_TIME , time:Time] |
    [TYPE_DATETIME, time:Time] |
    [TYPE_TIMESTAMP, time: Time] |
    [TYPE_ARRAY, anydata[]] |
    [TYPE_STRUCT, record{}] |
    [TYPE_REFCURSOR, typedesc<record {}>];

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

# Represents a parameter for the JDBC Client remote functions when a variable needs to be passed
# to the remote function.
#
# + sqlType - The data type of the corresponding SQL parameter
# + value - Value of paramter passed into the SQL statement
#                result row
public type TypedValue record {|
    SQLType sqlType;
    anydata value = ();
|};

# The parameter passed into the operations.
public type Value string|int|boolean|float|decimal|byte[]|TypedValue;
