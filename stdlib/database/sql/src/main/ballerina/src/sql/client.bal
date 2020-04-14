// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/java;

# Represents a SQL client.
#
public type Client abstract client object {

    # Queries the database with the query provided by the user, and returns the result as stream.
    #
    # + sqlQuery - The query which needs to be executed as `string` or `ParameterizedString` when the SQL query has
    #              params to be passed in
    # + rowType - The `typedesc` of the record that should be returned as a result. If this is not provided the default
    #             column names of the query result set be used for the record attributes
    # + return - Stream of records in the type of `rowType`
    public remote function query(@untainted string|ParameterizedString sqlQuery, typedesc<record {}>? rowType = ())
    returns @tainted stream<record{}, Error>;

    # Executes the DDL or DML sql queries provided by the user, and returns summary of the execution.
    #
    # + sqlQuery - The DDL or DML query such as INSERT, DELETE, UPDATE, etc
    # + return - Summary of the sql update query as `ExecuteResult` or returns `Error`
    #           if any error occured when executing the query
    public remote function execute(@untainted string sqlQuery) returns ExecuteResult|Error?;

    # Close the SQL client.
    #
    # + return - Possible error during closing the client
    public function close() returns Error?;
};

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
#
public type Type TYPE_VARCHAR|TYPE_CHAR|TYPE_LONGVARCHAR|TYPE_NCHAR|TYPE_LONGNVARCHAR|TYPE_NVARCHAR|TYPE_BIT|
TYPE_BOOLEAN|TYPE_TINYINT|TYPE_SMALLINT|TYPE_INTEGER|TYPE_BIGINT|TYPE_NUMERIC|TYPE_DECIMAL|TYPE_REAL|TYPE_FLOAT|
TYPE_DOUBLE|TYPE_BINARY|TYPE_BLOB|TYPE_LONGVARBINARY|TYPE_VARBINARY|TYPE_CLOB|TYPE_NCLOB|TYPE_DATE|TYPE_TIME|
TYPE_DATETIME|TYPE_TIMESTAMP|TYPE_ARRAY|TYPE_STRUCT;

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

# Represents a parameter for the SQL Client remote functions when a variable needs to be passed
# to the remote function.
#
# + sqlType - The data type of the corresponding SQL parameter
# + value - Value of paramter passed into the SQL statement
public type TypedValue record {|
   Type sqlType ;
   anydata|object{} value;
|};

# Possible type of parameters that can be passed into the SQL query.
public type Value string|int|boolean|float|decimal|byte[]|TypedValue;

# Temporay solution util the language supports `Backtick string` natively as mentioned in
# https://github.com/ballerina-platform/ballerina-spec/issues/442.
#
# + parts - The seperated parts of the sql query
# + insertions - The values that should be filled in between the parts
public type ParameterizedString record {|
   string[] parts;
   Value[] insertions;
|};

# The result of the query without returning the rows.
#
# + affectedRowCount - Number of rows affected by the execution of the query
# + lastInsertId - The integer or string generated by the database in response to a query execution.
#                  Typically this will be from an "auto increment" column when inserting a new row. Not all databases
#                  support this feature, and hence it can be also nil
public type ExecuteResult record {
    int? affectedRowCount;
    string|int? lastInsertId;
};

type ResultIterator object {
    private boolean isClosed = false;
    private Error? err;

    public function __init(public Error? err = ()) {
        self.err = err;
    }

    public function next() returns record {|record {} value;|}|Error? {
        if (self.isClosed) {
            return closedStreamInvocationError();
        }
        error? closeErrorIgnored = ();
        if (self.err is Error) {
            return self.err;
        } else {
            record {}|Error? result = nextResult(self);
            if (result is record {}) {
                record {|
                    record {} value;
                |} streamRecord = {value: result};
                return streamRecord;
            } else if (result is Error) {
                self.err = result;
                closeErrorIgnored = self.close();
                return self.err;
            } else {
                closeErrorIgnored = self.close();
                return result;
            }
        }
    }

    public function close() returns Error? {
        if (!self.isClosed) {
            if (self.err is ()) {
                Error? e = closeResult(self);
                if (e is ()) {
                    self.isClosed = true;
                }
                return e;
            }
        }
    }
};

function closedStreamInvocationError() returns Error {
    ApplicationError e = ApplicationError(message = "Stream is closed. Therefore, "
        + "no operations are allowed further on the stream.");
    return e;
}


public function generateApplicationErrorStream(string message) returns stream<record{}, Error> {
    ApplicationError applicationErr = ApplicationError(message = message);
    ResultIterator resultIterator = new (err = applicationErr);
    stream<record{}, Error> errorStream = new (resultIterator);
    return errorStream;
}

function nextResult(ResultIterator iterator) returns record {}|Error? = @java:Method {
    class: "org.ballerinalang.sql.utils.RecordItertorUtils"
} external;

function closeResult(ResultIterator iterator) returns Error? = @java:Method {
    class: "org.ballerinalang.sql.utils.RecordItertorUtils"
} external;
