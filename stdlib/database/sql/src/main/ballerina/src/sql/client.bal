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

import ballerinax/java;

# Represents a SQL client.
#
public type Client client object {

    public function query(@untainted string sqlQuery, typedesc<record {}>? rowType = ()) returns stream<record{}, error>{
        return nativeQuery(self, sqlQuery, rowType);
    }

    # Close the SQL client.
    #
    # + return - Possible error during closing the client
    public function close() returns error? {

    }
};

type ResultIterator object {
    boolean isClosed = false;

    public function next() returns record{| record{} value; |}| error? {
       if(self.isClosed) {
            return closedStreamInvocationError();
       }
       record{}| error? result =  nextResult(self);
       if(result is record{} ){
            record {|
               record{} value;
           |} streamRecord = { value: result };
           return streamRecord;
       } else {
          return result;
       }
    }

    public function close() returns error? {
        return closeResult(self);
    }
};

function nativeQuery(Client sqlClient, @untainted string sqlQuery, typedesc<record {}>? rowtype) returns stream<record{}, error> = @java:Method {
    class: "org.ballerinalang.sql.utils.QueryUtils"
} external;

function closedStreamInvocationError() returns Error {
    ApplicationError e = ApplicationError(message = "Stream is closed. Therefore, no operations are allowed further on the stream.");
    return e;
}

function nextResult(ResultIterator iterator) returns record{}|error? = @java:Method {
    class: "org.ballerinalang.sql.utils.QueryUtils"
} external;

function closeResult(ResultIterator iterator) returns error? = @java:Method {
    class: "org.ballerinalang.sql.utils.QueryUtils"
} external;
