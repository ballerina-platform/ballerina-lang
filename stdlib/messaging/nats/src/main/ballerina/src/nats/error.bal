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

# Holds the details of an error.
#
# + message - Specific error message of the error
# + cause - Any other error, which causes this error
public type Detail record {
    string message;
    error cause?;
};

# Represents the reason for the NATS module related errors.
public const NATS_ERROR = "{ballerina/nats}Error";

# Represents the NATS module related errors.
public type Error error<NATS_ERROR, Detail>;

# Prepare the `error` as a `Error`.
#
# + message - The error message
# + err - The `error` instance
# + return - Prepared `nats:Error` instance
function prepareError(string message, error? err = ()) returns Error {
    Error natsError;
    if (err is error) {
        natsError = error(NATS_ERROR, message = message, cause = err);
    } else {
        natsError = error(NATS_ERROR, message = message);
    }
    return natsError;
}
