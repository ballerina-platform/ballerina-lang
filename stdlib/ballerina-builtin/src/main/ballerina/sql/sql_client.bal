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

package ballerina.sql;

@Description {value:"Parameter represents a query parameter for the SQL queries specified in connector actions"}
@Field {value:"sqlType: The data type of the corresponding SQL parameter"}
@Field {value:"value: Value of paramter pass into the SQL query"}
@Field {value:"direction: Direction of the SQL Parameter IN, OUT, or INOUT"}
@Field {value:"recordType: In case of OUT direction, if the sqlType is REFCURSOR, this represents the record type to
map a result row"}
type ParameterType {
    SQLType sqlType,
    any value,
    Direction direction,
    typedesc recordType,
};

@Description {value:"The SQL Datatype of the parameter"}
@Field {value:"VARCHAR: Small, variable-length character string"}
@Field {value:"CHAR: Small, fixed-length character string"}
@Field {value:"LONGVARCHAR: Large, variable-length character string"}
@Field {value:"NCHAR: Small, fixed-length character string with unicode support"}
@Field {value:"LONGNVARCHAR: Large, variable-length character string with unicode support"}
@Field {value:"BIT: Single bit value that can be zero or one, or null"}
@Field {value:"BOOLEAN: Boolean value either True or false"}
@Field {value:"TINYINT: 8-bit integer value which may be unsigned or signed"}
@Field {value:"SMALLINT: 16-bit signed integer value which may be unsigned or signed"}
@Field {value:"INTEGER: 32-bit signed integer value which may be unsigned or signed"}
@Field {value:"BIGINT: 64-bit signed integer value which may be unsigned or signed"}
@Field {value:"NUMERIC: Fixed-precision and scaled decimal values"}
@Field {value:"DECIMAL: Fixed-precision and scaled decimal values"}
@Field {value:"REAL: Single precision floating point number"}
@Field {value:"FLOAT: Double precision floating point number"}
@Field {value:"DOUBLE: Double precision floating point number"}
@Field {value:"BINARY: Small, fixed-length binary value"}
@Field {value:"BLOB: Binary Large Object"}
@Field {value:"LONGVARBINARY: Large, variable-length binary value"}
@Field {value:"VARBINARY: Small, variable-length binary value"}
@Field {value:"CLOB: Character Large Object"}
@Field {value:"NCLOB: Character large objects in multibyte national character set"}
@Field {value:"DATE: Date consisting of day, month, and year"}
@Field {value:"TIME: Time consisting of hours, minutes, and seconds"}
@Field {value:"DATETIME: Both DATE and TIME with additional a nanosecond field"}
@Field {value:"TIMESTAMP: Both DATE and TIME with additional a nanosecond field"}
@Field {value:"ARRAY: Composite data value that consists of zero or more elements of a specified data type"}
@Field {value:"STRUCT: User defined structured type, consists of one or more attributes"}
public type SQLType
"VARCHAR"|
"CHAR"|
"LONGVARCHAR"|
"NCHAR"|
"LONGNVARCHAR"|
"NVARCHAR"|
"BIT"|
"BOOLEAN"|
"TINYINT"|
"SMALLINT"|
"INTEGER"|
"BIGINT"|
"NUMERIC"|
"DECIMAL"|
"REAL"|
"FLOAT"|
"DOUBLE"|
"BINARY"|
"BLOB"|
"LONGVARBINARY"|
"VARBINARY"|
"CLOB"|
"NCLOB"|
"DATE"|
"TIME"|
"DATETIME"|
"TIMESTAMP"|
"ARRAY"|
"STRUCT"|
"REFCURSOR";

@final public SQLType TYPE_VARCHAR = "VARCHAR";
@final public SQLType TYPE_CHAR = "CHAR";
@final public SQLType TYPE_LONGVARCHAR = "LONGVARCHAR";
@final public SQLType TYPE_NCHAR = "NCHAR";
@final public SQLType TYPE_LONGNVARCHAR = "LONGNVARCHAR";
@final public SQLType TYPE_NVARCHARR = "NVARCHAR";
@final public SQLType TYPE_BIT = "BIT";
@final public SQLType TYPE_BOOLEAN = "BOOLEAN";
@final public SQLType TYPE_TINYINT = "TINYINT";
@final public SQLType TYPE_SMALLINT = "SMALLINT";
@final public SQLType TYPE_INTEGER = "INTEGER";
@final public SQLType TYPE_BIGINT = "BIGINT";
@final public SQLType TYPE_NUMERIC = "NUMERIC";
@final public SQLType TYPE_DECIMAL = "DECIMAL";
@final public SQLType TYPE_REAL = "REAL";
@final public SQLType TYPE_FLOAT = "FLOAT";
@final public SQLType TYPE_DOUBLE = "DOUBLE";
@final public SQLType TYPE_BINARY = "BINARY";
@final public SQLType TYPE_BLOB = "BLOB";
@final public SQLType TYPE_LONGVARBINARY = "LONGVARBINARY";
@final public SQLType TYPE_VARBINARY = "VARBINARY";
@final public SQLType TYPE_CLOB = "CLOB";
@final public SQLType TYPE_NCLOB = "NCLOB";
@final public SQLType TYPE_DATE = "DATE";
@final public SQLType TYPE_TIME = "TIME";
@final public SQLType TYPE_DATETIME = "DATETIME";
@final public SQLType TYPE_TIMESTAMP = "TIMESTAMP";
@final public SQLType TYPE_ARRAY = "ARRAY";
@final public SQLType TYPE_STRUCT = "STRUCT";
@final public SQLType TYPE_REFCURSOR = "REFCURSOR";


@Description {value:"The direction of the parameter"}
@Field {value:"IN: IN parameters are used to send values to stored procedures"}
@Field {value:"OUT: OUT parameters are used to get values from stored procedures"}
@Field {value:"INOUT: INOUT parameters are used to send values and get values from stored procedures"}
public type Direction
"IN"|
"OUT"|
"INOUT";

@final public Direction DIRECTION_IN = "IN";
@final public Direction DIRECTION_OUT = "OUT";
@final public Direction DIRECTION_INOUT = "INOUT";

@Description {value:"Parameter represents a query parameter for the SQL queries specified in connector actions"}
@Field {value:"sqlType: The data type of the corresponding SQL parameter"}
@Field {value:"value: Value of paramter pass into the SQL query"}
@Field {value:"direction: Direction of the SQL Parameter IN, OUT, or INOUT"}
@Field {value:"recordType: In case of OUT direction, if the sqlType is REFCURSOR, this represents the record type to
map a result row"}
public type CallParam {
    SQLType sqlType,
    any value,
    Direction direction,
    typedesc recordType,
};

public type Parameter
(SQLType, any, Direction, typedesc)|
(SQLType, any, Direction)|
(SQLType, any)|
CallParam|//To used with the SQL out parameters
any;
///////////////////////////////
// SQL Client Connector
///////////////////////////////

@Description {value:"The Client Connector for SQL databases."}
public type SQLClient object {

    @Description {value:"The call action implementation for SQL connector to invoke stored procedures/functions."}
    @Param {value:"sqlQuery: SQL query to execute"}
    @Param {value:"parameters: Parameters used with the SQL query"}
    @Return {value:"Result set(s) for the given query"}
    @Return {value:"The Error occured during SQL client invocation"}
    public native function call(@sensitive string sqlQuery, (typedesc[]|()) recordType, Parameter... parameters)
        returns @tainted (table[]|error);

    @Description {value:"The select action implementation for SQL connector to select data from tables."}
    @Param {value:"sqlQuery: SQL query to execute"}
    @Param {value:"parameters: Parameters used with the SQL query"}
    @Return {value:"Result set for the given query"}
    @Return {value:"The Error occured during SQL client invocation"}
    public native function select(@sensitive string sqlQuery, (typedesc|()) recordType, Parameter... parameters)
        returns @tainted (table|error);

    @Description {value:"The close action implementation for SQL connector to shutdown the connection pool."}
    @Return {value:"The Error occured during SQL client invocation"}
    public native function close() returns (error|());

    @Description {value:"The update action implementation for SQL connector to update data and schema of the database."}
    @Param {value:"sqlQuery: SQL query to execute"}
    @Param {value:"parameters: Parameters used with the SQL query"}
    @Return {value:"Updated row count"}
    @Return {value:"The Error occured during SQL client invocation"}
    public native function update(@sensitive string sqlQuery, Parameter... parameters)
        returns (int|error);

    @Description {value:"The batchUpdate action implementation for SQL connector to batch data insert."}
    @Param {value:"sqlQuery: SQL query to execute"}
    @Param {value:"parameters: Parameters used with the SQL query"}
    @Return {value:"Array of update counts"}
    @Return {value:"The Error occured during SQL client invocation"}
    public native function batchUpdate(@sensitive string sqlQuery, Parameter[]... parameters)
        returns (int[]|error);

    @Description {value:"The updateWithGeneratedKeys action implementation for SQL connector which returns the auto
        generated keys during the update action."}
    @Param {value:"sqlQuery: SQL query to execute"}
    @Param {value:"parameters: Parameters used with the SQL query"}
    @Param {value:"keyColumns: Names of auto generated columns for which the auto generated key values are returned"}
    @Return {value:"Updated row count during the query exectuion"}
    @Return {value:"Array of auto generated key values during the query execution"}
    @Return {value:"The Error occured during SQL client invocation"}
    public native function updateWithGeneratedKeys(@sensitive string sqlQuery,
    (string[]|()) keyColumns, Parameter... parameters)
        returns (int, string[])|error;

    @Description {value:"The mirror action implementation for SQL connector which returns a reflection of a database
    table that allows performing select/update operations over the actual database table"}
    @Param {value:"tableName: The name of the table to be mirrored"}
    @Param {value:"recordType: The type which a record of the table maps with"}
    public native function mirror(@sensitive string tableName, typedesc recordType) returns
        @tainted (table|error);

};
