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

@Description {value:"Execute the given sql query to fetch the records and return as a new in memory table"}
@Param {value:"sqlQuery: The query to execute"}
@Param {value:"fromTable: The table on which the query is executed"}
@Param {value:"joinTable: The table which is joined with 'fromTable'"}
@Param {value:"parameters: liternal parameters to be passed to prepared statement 'sqlQuery'"}
@Param {value:"retType: return type of the resultant table instance"}
public native function queryTableWithJoinClause (string sqlQuery, table fromTable, table joinTable, any parameters,
                                                 any retType) (table);

@Description {value:"Execute the given sql query to fetch the records and return as a new in memory table"}
@Param {value:"sqlQuery: The query to execute"}
@Param {value:"fromTable: The table on which the query is executed"}
@Param {value:"parameters: literal parameters to be passed to prepared statement 'sqlQuery'"}
@Param {value:"retType: return type of the resultant table instance"}
public native function queryTableWithoutJoinClause (string sqlQuery, table fromTable, any parameters,
                                                    any retType) (table);