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

public type Client abstract client object {

    # The call remote function implementation for SQL Client to invoke stored procedures/functions.
    #
    # + sqlQuery - The SQL query such as SELECT statements which returns the table rows.
    # + params - The parameters to be passed to query.
    # + rowType - The type description of the record that should be returns in the retuned stream.
    # + return - A `stream<record{}>` containing the records of the query results.
    public remote function query(@untainted string sqlQuery, Value[]? params = (), typedesc<record {}>? rowType =())
                                returns @tainted stream<record {}>;

    # Close the SQL client.
    #
    # + return - Possible error during closing the client
    public function close() returns error?;
};
