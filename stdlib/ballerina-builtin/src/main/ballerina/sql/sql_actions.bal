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

documentation {
    The Caller actions for SQL databases.
}
public type CallerActions object {

    documentation {
        The call action implementation for SQL connector to invoke stored procedures/functions.

        P{{sqlQuery}} - SQL statement to execute.
        P{{recordType}} - Array of record types of the returned tables if there is any.
        R{{}} -  `table[]` if there are tables returned by the call action and else nill,
                `error` will be returned if there is any error.
    } //Returns array of tables if there are any.
    public native function call(@sensitive string sqlQuery, typedesc[]? recordType, Parameter... parameters)
        returns @tainted table[]|error;

    documentation {
        The select action implementation for SQL connector to select data from tables.

        P{{sqlQuery}} - SQL query to execute.
        P{{recordType}} - Type of the returned table.
        R{{}} - `table` table returned by the sql query statement else `error` will be returned if there is any error.
    }
    public native function select(@sensitive string sqlQuery, typedesc? recordType, Parameter... parameters)
        returns @tainted table|error;

    documentation {
        The close action implementation for SQL connector to shutdown the connection pool.
        R{{}} - `error` will be returned if there is any error.
    }
    public native function close() returns (error?);


    documentation {
        The update action implementation for SQL connector to update data and schema of the database.

        P{{sqlQuery}} - SQL statement to execute.
        R{{}} - `int` number of rows updated by the statement and else `error` will be returned if there is any error.

    }
    public native function update(@sensitive string sqlQuery, Parameter... parameters) returns int|error;


    documentation {
        The batchUpdate action implementation for SQL connector to batch data insert.

        P{{sqlQuery}} - SQL statement to execute.
        R{{}} - `int[]` An array of updated row count by each of statements in batch and
                else `error` will be returned if there is any error.
    }
    public native function batchUpdate(@sensitive string sqlQuery, Parameter[]... parameters) returns int[]|error;


    documentation {
        The updateWithGeneratedKeys action implementation for SQL connector which returns the auto
        generated keys during the update action.

        P{{sqlQuery}} - SQL statement to execute.
        P{{keyColumns}} - Names of auto generated columns for which the auto generated key values are returned.
        R{{}} - A `Tuple` will be returned and would represent updated row count during the query exectuion,
            aray of auto generated key values during the query execution, in order.
            Else `error` will be returned if there is any error.

    }
    public native function updateWithGeneratedKeys(@sensitive string sqlQuery, string[]? keyColumns,
                                                   Parameter... parameters) returns (int, string[])|error;

    documentation {
        The mirror action implementation for SQL connector which returns a reflection of a database
        table that allows performing select/update operations over the actual database table.

        P{{tableName}} - The name of the table to be mirrored.
        P{{recordType}} - The record type of the returned table.

    }
    public native function mirror(@sensitive string tableName, typedesc recordType) returns @tainted table|error;
};
