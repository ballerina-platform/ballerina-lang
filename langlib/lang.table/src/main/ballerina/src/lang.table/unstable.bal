// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
#
# + tbl - table to operate on
public function close(table<RowType> tbl) = external;

# Checks for a new row in the given table. If a new row is found, moves the cursor to it.
#
# + tbl - table to operate on
# + return - True if there is a new row; false otherwise
public function hasNext(table<RowType> tbl) returns boolean = external;

# Retrives the current row and return a record with the data in the columns.
#
# + tbl - table to operate on
# + return - The resulting row as a record
public function getNext(table<RowType> tbl) returns RowType = external;

# Add record to the table.
#
# + tbl - table to operate on
# + data - A record with data
# + return - An `error` will be returned if there is any error occurred during adding data or else nil is returned
public function add(table<RowType> tbl, RowType data) returns error|() = external;

# Remove data from the table.
#
# + tbl - table to operate on
# + func - The function pointer for delete crieteria
# + return - An `int` the number of deleted record count or `error` if any error occurred during removing data
public function remove(table<RowType> tbl, function (RowType) returns (boolean) func) returns int|error = external;

# Execute the given sql query to fetch the records and return as a new in memory table.
#
# + sqlQuery - The query to execute
# + fromTable - The table on which the query is executed
# + joinTable - The table which is joined with 'fromTable'
# + parameters - liternal parameters to be passed to prepared statement 'sqlQuery'
# + retType - return type of the resultant table instance
# + return - table value
function queryTableWithJoinClause(string sqlQuery, table<RowType> fromTable, table<RowType> joinTable, any parameters,
                                  any retType) returns table<RowType> = external;

# Execute the given sql query to fetch the records and return as a new in memory table.
#
# + sqlQuery - The query to execute
# + fromTable - The table on which the query is executed
# + parameters - literal parameters to be passed to prepared statement 'sqlQuery'
# + retType - return type of the resultant table instance
# + return - table value
function queryTableWithoutJoinClause(string sqlQuery, table<RowType> fromTable, any parameters,
                                     any retType) returns table<RowType> = external;
