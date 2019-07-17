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

# Holds the details of an gRPC error
#
# + message - Specific error message for the error
# + cause - Cause of the error; If this error occurred due to another error (Probably from another module)
public type Detail record {
    string message;
    error cause?;
};

# Generic errors (mostly to wrap errors from other modules)
public const CLIENT_ERROR = "{ballerina/grpc}ClientError";
public type ClientError error<CLIENT_ERROR, Detail>;

public const LISTENER_ERROR = "{ballerina/grpc}ListenerError";
public type ListenerError error<LISTENER_ERROR, Detail>;

public type Error ClientError|ListenerError;
