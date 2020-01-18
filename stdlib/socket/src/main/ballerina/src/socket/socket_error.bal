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

# Record type to hold the details of an error.
#
# + message - Specific error message of the error.
# + cause - Any other error, which causes this error.
public type Detail record {
    string message;
    error cause?;
};

# This will used to construct a ReadTimedOutError.
public const READ_TIMED_OUT = "{ballerina/socket}ReadTimedOut";
# This will returns once the given read timed out time exceed for socket reads.
public type ReadTimedOutError error<READ_TIMED_OUT, Detail>;

# This will used to construct a GENERIC_ERROR.
public const GENERIC_ERROR = "{ballerina/socket}GenericError";
# Represents generic socket error. The detail record contains the information related to the error.
public type GenericError error<GENERIC_ERROR, Detail>;

# Represents socket module related errors.
public type Error GenericError|ReadTimedOutError;
