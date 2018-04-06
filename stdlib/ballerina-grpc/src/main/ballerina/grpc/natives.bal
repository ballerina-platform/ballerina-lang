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
package ballerina.grpc;

documentation {
    ConnectorError struct represents an error occured during the HTTP client invocation

    F{{message}} - An error message explaining about the error.
    F{{cause}} - The error that caused ConnectorError to get thrown.
    F{{statusCode}} - gRPC status code.
}
public type ConnectorError object {
    public {
        string message;
        error cause;
        int statusCode;
    }
}

documentation {
    ServerError struct represents an error occured during gRPC server excution

    F{{message}} - An error message explaining about the error.
    F{{cause}} - The error that caused ServerError to get thrown.
    F{{statusCode}} - gRPC server status code. refer: https://github
.com/grpc/grpc-java/blob/master/core/src/main/java/io/grpc/Status.java.
}
public type ServerError object {
    public {
        string message;
        error cause;
        int statusCode;
    }
}

documentation {
    ClientError struct represents an error occured during gRPC client connector

    F{{message}} - An error message explaining about the error.
    F{{cause}} - The error that caused ClientError to get thrown.
    F{{statusCode}} - gRPC server status code. refer: https://github
.com/grpc/grpc-java/blob/master/core/src/main/java/io/grpc/Status.java.
}
public type ClientError object {
    public {
        string message;
        error cause;
        int statusCode;
    }
}
