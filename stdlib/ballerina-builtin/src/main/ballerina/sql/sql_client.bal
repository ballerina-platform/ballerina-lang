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

@Description {value:"Parameter struct represents a query parameter for the SQL queries specified in connector actions"}
@Field {value:"sqlType: The data type of the corresponding SQL parameter"}
@Field {value:"value: Value of paramter pass into the SQL query"}
@Field {value:"direction: Direction of the SQL Parameter IN, OUT, or INOUT"}
@Field {value:"structType: In case of OUT direction, if the sqlType is REFCURSOR, this represents the struct type to
map a result row"}
public type Parameter {
    Type sqlType,
    any value,
    Direction direction,
    typedesc structType,
}

@Description {value:"The Databases which has direct parameter support."}
@Field {value:"MYSQL: MySQL DB with connection url in the format of  jdbc:mysql://[HOST]:[PORT]/[database]"}
@Field {value:"SQLSERVER: SQL Server DB with connection url in the format of jdbc:sqlserver://[HOST]:[PORT];databaseName=[database]"}
@Field {value:"ORACLE: Oracle DB with connection url in the format of  jdbc:oracle:thin:[username/password]@[HOST]:[PORT]/[database]"}
@Field {value:"SYBASE: Sybase DB with connection url in the format of  jdbc:sybase:Tds:[HOST]:[PORT]/[database]"}
@Field {value:"POSTGRES: PostgreSQL DB with connection url in the format of  jdbc:postgresql://[HOST]:[PORT]/[database]"}
@Field {value:"IBMDB2: IBMDB2 DB with connection url in the format of  jdbc:db2://[HOST]:[PORT]/[database]"}
@Field {value:"HSQLDB_SERVER: HSQL Server with connection url in the format of jdbc:hsqldb:hsql://[HOST]:[PORT]/[database]"}
@Field {value:"HSQLDB_FILE: HSQL Server with connection url in the format of jdbc:hsqldb:file:[path]/[database]"}
@Field {value:"H2_SERVER: H2 Server DB with connection url in the format of jdbc:h2:tcp://[HOST]:[PORT]/[database]"}
@Field {value:"H2_FILE: H2 File DB with connection url in the format of jdbc:h2:file://[path]/[database]"}
@Field {value:"H2_MEM: H2 in memory DB with connection url in the format of jdbc:h2:mem:[database]"}
@Field {value:"DERBY_SERVER: DERBY server DB with connection url in the format of jdbc:derby://[HOST]:[PORT]/[database]"}
@Field {value:"DERBY_FILE: Derby file DB with connection url in the format of jdbc:derby://[path]/[database]"}
@Field {value:"GENERIC: Custom DB connection with given connection url"}
public type DB "DB_MYSQL"|
    "DB_SQLSERVER"|
    "DB_ORACLE"|
    "DB_SYBASE"|
    "DB_POSTGRES"|
    "DB_IBMDB2"|
    "DB_HSQLDB_SERVER"|
    "DB_HSQLDB_FILE"|
    "DB_H2_SERVER"|
    "DB_H2_FILE"|
    "DB_H2_MEM"|
    "DB_DERBY_SERVER"|
    "DB_DERBY_FILE"|
    "DB_GENERIC";

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
public type Type
    "TYPE_VARCHAR"|
    "TYPE_CHAR"|
    "TYPE_LONGVARCHAR"|
    "TYPE_NCHAR"|
    "TYPE_LONGNVARCHAR"|
    "TYPE_NVARCHAR"|
    "TYPE_BIT"|
    "TYPE_BOOLEAN"|
    "TYPE_TINYINT"|
    "TYPE_SMALLINT"|
    "TYPE_INTEGER"|
    "TYPE_BIGINT"|
    "TYPE_NUMERIC"|
    "TYPE_DECIMAL"|
    "TYPE_REAL"|
    "TYPE_FLOAT"|
    "TYPE_DOUBLE"|
    "TYPE_BINARY"|
    "TYPE_BLOB"|
    "TYPE_LONGVARBINARY"|
    "TYPE_VARBINARY"|
    "TYPE_CLOB"|
    "TYPE_NCLOB"|
    "TYPE_DATE"|
    "TYPE_TIME"|
    "TYPE_DATETIME"|
    "TYPE_TIMESTAMP"|
    "TYPE_ARRAY"|
    "TYPE_STRUCT"|
    "TYPE_REFCURSO";

@Description {value:"The direction of the parameter"}
@Field {value:"IN: IN parameters are used to send values to stored procedures"}
@Field {value:"OUT: OUT parameters are used to get values from stored procedures"}
@Field {value:"INOUT: INOUT parameters are used to send values and get values from stored procedures"}
public type Direction
    "DIRECTION_IN"|,
    "DIRECTION_OUT"|,
    "DIRECTION_INOUT";

///////////////////////////////
// SQL Client Connector
///////////////////////////////

@Description {value:"The Client Connector for SQL databases."}
public type SQLClient object {

    @Description {value:"The call action implementation for SQL connector to invoke stored procedures/functions."}
    @Param {value:"sqlQuery: SQL query to execute"}
    @Param {value:"parameters: Parameter array used with the SQL query"}
    @Return {value:"Result set(s) for the given query"}
    @Return {value:"The Error occured during SQL client invocation"}
    public native function call (@sensitive string sqlQuery, (Parameter[] | null) parameters,
        (typedesc | null) structType)
        returns @tainted (table[] | SQLConnectorError);

    @Description {value:"The select action implementation for SQL connector to select data from tables."}
    @Param {value:"sqlQuery: SQL query to execute"}
    @Param {value:"parameters: Parameter array used with the SQL query"}
    @Return {value:"Result set for the given query"}
    @Return {value:"The Error occured during SQL client invocation"}
    public native function select (@sensitive string sqlQuery, (Parameter[] | null) parameters,
        (typedesc | null) structType)
        returns @tainted (table | SQLConnectorError);

    @Description {value:"The close action implementation for SQL connector to shutdown the connection pool."}
    @Return {value:"The Error occured during SQL client invocation"}
    public native function close() returns (SQLConnectorError | null);

    @Description {value:"The update action implementation for SQL connector to update data and schema of the database."}
    @Param {value:"sqlQuery: SQL query to execute"}
    @Param {value:"parameters: Parameter array used with the SQL query"}
    @Return {value:"Updated row count"}
    @Return {value:"The Error occured during SQL client invocation"}
    public native function update (@sensitive string sqlQuery, (Parameter [] | null) parameters)
        returns (int | SQLConnectorError);

    @Description {value:"The batchUpdate action implementation for SQL connector to batch data insert."}
    @Param {value:"sqlQuery: SQL query to execute"}
    @Param {value:"parameters: Parameter array used with the SQL query"}
    @Return {value:"Array of update counts"}
    @Return {value:"The Error occured during SQL client invocation"}
    public native function batchUpdate (@sensitive string sqlQuery, (Parameter[][]|null) parameters)
        returns (int[] | SQLConnectorError);

    @Description {value:"The updateWithGeneratedKeys action implementation for SQL connector which returns the auto
        generated keys during the update action."}
    @Param {value:"sqlQuery: SQL query to execute"}
    @Param {value:"parameters: Parameter array used with the SQL query"}
    @Param {value:"keyColumns: Names of auto generated columns for which the auto generated key values are returned"}
    @Return {value:"Updated row count during the query exectuion"}
    @Return {value:"Array of auto generated key values during the query execution"}
    @Return {value:"The Error occured during SQL client invocation"}
    public native function updateWithGeneratedKeys (@sensitive string sqlQuery,
        (Parameter[] | null) parameters, (string[] | null) keyColumns)
        returns (int, string[]) | SQLConnectorError;
}

@Description { value:"SQLConnectorError struct represents an error occured during the SQL client invocation" }
@Field {value:"message:  An error message explaining about the error"}
@Field {value:"cause: The error(s) that caused SQLConnectorError to get thrown"}
public type SQLConnectorError {
    string message,
    error[] cause,
}
