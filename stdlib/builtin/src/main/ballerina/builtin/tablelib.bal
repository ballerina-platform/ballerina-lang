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

# Releases the database connection. If the table data is fully iterated, it will be automatically closed. This explicit
# close is required only if it is not fully iterated.
public extern function table::close();

# Checks for a new row in the given table. If a new row is found, moves the cursor to it.
#
# + return - True if there is a new row; false otherwise
public extern function table::hasNext() returns boolean;

# Retrives the current row and return a record with the data in the columns.
#
# + return - The resulting row as a record
public extern function table::getNext() returns any;

# Add record to the table.
#
# + data - A record with data
# + return - An `error` will be returned if there is any error occured during adding data or else nil is returned
public extern function table::add(any data) returns error|();

# Remove data from the table.
#
# + func - The function pointer for delete crieteria
# + return - An `int` the number of deleted record count or `error` if any error occured during removing data
public extern function table::remove(function (any) returns (boolean) func) returns int|error;

# Execute the given sql query to fetch the records and return as a new in memory table.
#
# + sqlQuery - The query to execute
# + fromTable - The table on which the query is executed
# + joinTable - The table which is joined with 'fromTable'
# + parameters - liternal parameters to be passed to prepared statement 'sqlQuery'
# + retType - return type of the resultant table instance
extern function queryTableWithJoinClause(string sqlQuery, table fromTable, table joinTable, any parameters,
                                         any retType) returns table;

# Execute the given sql query to fetch the records and return as a new in memory table.
#
# + sqlQuery - The query to execute
# + fromTable - The table on which the query is executed
# + parameters - literal parameters to be passed to prepared statement 'sqlQuery'
# + retType - return type of the resultant table instance
extern function queryTableWithoutJoinClause(string sqlQuery, table fromTable, any parameters,
                                            any retType) returns table;

# TableConfig represents properties used during table initialization.
#
# + primaryKey - An array of primary key columns
# + index - An array of index columns
# + data - An array of record data
type TableConfig record {
    string[] primaryKey;
    string[] index;
    any[] data;
    !...
};
