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

package ballerina.builtin;

@Description {value:"Releases the database connection."}
@Param {value:"dt: The table object"}
public native function <table dt> close ();

@Description {value:"Checks for a new row in the given table. If a new row is found, moves the cursor to it."}
@Param {value:"dt: The table object"}
@Return {value:"True if there is a new row; false otherwise"}
public native function <table dt> hasNext () returns (boolean);

@Description {value:"Retrives the current row and return a struct with the data in the columns"}
@Param {value:"dt: The table object"}
@Return {value:"The resulting row as a struct"}
public native function <table dt> getNext () returns (any);

@Description {value:"Add struct to the table."}
@Param {value:"dt: The table object"}
@Param {value:"data: A struct with data"}
public native function <table dt> add (any data);

@Description {value:"Remove data from the table."}
@Param {value:"dt: The table object"}
@Param {value:"func: The function pointer for delete crieteria"}
public function <table dt> remove (function (any) returns (boolean) func) returns (int) {
    int deletedCount = 0;
    while (dt.hasNext()) {
        any data = dt.getNext();
        boolean satisfied = func(data);
        if (satisfied) {
            dt.delete(data);
            deletedCount = deletedCount + 1;
        }
    }
    return deletedCount;
}

@Description {value:"Utility function to delete data from table."}
@Param {value:"dt: The table object"}
@Param {value:"data: A struct with data"}
native function <table dt> delete (any data);

@Description {value:"Execute the given sql query to fetch the records and return as a new in memory table"}
@Param {value:"sqlQuery: The query to execute"}
@Param {value:"fromTable: The table on which the query is executed"}
@Param {value:"joinTable: The table which is joined with 'fromTable'"}
@Param {value:"parameters: liternal parameters to be passed to prepared statement 'sqlQuery'"}
@Param {value:"retType: return type of the resultant table instance"}
native function queryTableWithJoinClause (string sqlQuery, table fromTable, table joinTable, any parameters,
                                                 any retType) returns (table);

@Description {value:"Execute the given sql query to fetch the records and return as a new in memory table"}
@Param {value:"sqlQuery: The query to execute"}
@Param {value:"fromTable: The table on which the query is executed"}
@Param {value:"parameters: literal parameters to be passed to prepared statement 'sqlQuery'"}
@Param {value:"retType: return type of the resultant table instance"}
native function queryTableWithoutJoinClause (string sqlQuery, table fromTable, any parameters,
                                                    any retType) returns (table);

@Description { value:"TableConfig represents properties used during table initialization" }
@Field {value:"primaryKey:  An array of primary key columns"}
@Field {value:"index: An array of index columns"}
@Field {value:"data: An array of struct data"}
type TableConfig {
    string[] primaryKey;
    string[] index;
    any[] data;
};
