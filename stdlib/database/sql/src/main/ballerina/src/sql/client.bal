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
public type Client abstract client object {

    # Executes the sql query provided by the user, and returns the result as stream.
    #
    # + sqlQuery - The query which needs to be executed
    # + rowType - The `typedesc` of the record that should be returned as a result. If this is not provided the default
    #             column names of the query result set be used for the record attributes
    # + return - Stream of records in the type of `rowType`
    public function query(@untainted string sqlQuery, typedesc<record {}>? rowType = ()) returns stream<record{}, error>;

    # Close the SQL client.
    #
    # + return - Possible error during closing the client
    public function close() returns Error?;
};

type ResultIterator object {
    private boolean isClosed = false;
    private Error? err;

    public function __init(public Error? err = ()) {
        self.err = err;
    }

    public function next() returns record{| record{} value; |}| error? {
       if(self.isClosed) {
            return closedStreamInvocationError();
       }
       error? closeErrorIgnored = ();
       if(self.err is Error) {
         closeErrorIgnored = self.close();
         return self.err;
       } else {
           record{}| Error? result =  nextResult(self);
           if(result is record{}){
                record {|
                   record{} value;
               |} streamRecord = { value: result };
               return streamRecord;
           } else if (result is Error ){
              self.err = result;
              closeErrorIgnored = self.close();
              return self.err;
           } else {
              closeErrorIgnored = self.close();
              return result;
           }
       }
    }

    public function close() returns error? {
    if(!self.isClosed){
            if (self.err is ()){
                return closeResult(self);
            }
        }
    }
};

function closedStreamInvocationError() returns Error {
    ApplicationError e = ApplicationError(message = "Stream is closed. Therefore, no operations are allowed further on the stream.");
    return e;
}

public function generateApplicationError (string message) returns stream<record{}, error> {
    ApplicationError applicationErr = ApplicationError(message = message);
    ResultIterator resultIterator = new (err = applicationErr);
    stream<record{}, error> errorStream = new (resultIterator);
    return errorStream;
}

function nextResult(ResultIterator iterator) returns record{}|Error? = @java:Method {
    class: "org.ballerinalang.sql.utils.RecordItertorUtils"
} external;

function closeResult(ResultIterator iterator) returns Error? = @java:Method {
    class: "org.ballerinalang.sql.utils.RecordItertorUtils"
} external;
