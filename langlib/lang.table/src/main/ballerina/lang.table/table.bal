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

# The type bound for a table row.
@typeParam
type RowType map<anydata|error>;

type TableIterator object {

    private table <anydata|error> m;

    public function __init(table<anydata|error> m) {
        self.m = m;
    }

    public function next() returns record {|
        RowType value;
    |}? = external;
};

# Returns an iterator over the members of `tbl`.
public function iterator(table<RowType> tbl) returns abstract object {
    public function next() returns record {|
        RowType value;
    |}?;
    } {
    TableIterator tableIterator = new(tbl);
    return tableIterator;
}

# Releases the database connection. If the table data is fully iterated, it will be automatically closed. This explicit
# close is required only if it is not fully iterated.
public function close(table<record {}> dt) = external;

# Checks for a new row in the given table. If a new row is found, moves the cursor to it.
#
# + return - True if there is a new row; false otherwise
public function hasNext(table<record {}> dt) returns boolean = external;

# Retrives the current row and return a record with the data in the columns.
#
# + return - The resulting row as a record
public function getNext(table<record {}> dt) returns any = external;

# Add record to the table.
#
# + data - A record with data
# + return - An `error` will be returned if there is any error occurred during adding data or else nil is returned
public function add(table<record {}> dt, any data) returns error|() = external;

# Remove data from the table.
#
# + func - The function pointer for delete crieteria
# + return - An `int` the number of deleted record count or `error` if any error occurred during removing data
public function remove(table<record {}> dt, function (any) returns (boolean) func) returns int|error = external;

# Execute the given sql query to fetch the records and return as a new in memory table.
#
# + sqlQuery - The query to execute
# + fromTable - The table on which the query is executed
# + joinTable - The table which is joined with 'fromTable'
# + parameters - liternal parameters to be passed to prepared statement 'sqlQuery'
# + retType - return type of the resultant table instance
function queryTableWithJoinClause(string sqlQuery, table<record {}> fromTable, table<record {}> joinTable, any parameters,
                                  any retType) returns table<record {}> = external;

# Execute the given sql query to fetch the records and return as a new in memory table.
#
# + sqlQuery - The query to execute
# + fromTable - The table on which the query is executed
# + parameters - literal parameters to be passed to prepared statement 'sqlQuery'
# + retType - return type of the resultant table instance
function queryTableWithoutJoinClause(string sqlQuery, table<record {}> fromTable, any parameters,
                                     any retType) returns table<record {}> = external;

# TableConfig represents properties used during table initialization.
#
# + primaryKey - An array of primary key columns
# + index - An array of index columns
# + data - An array of record data
type TableConfig record {|
    string[] primaryKey;
    string[] index;
    any[] data;
|};
