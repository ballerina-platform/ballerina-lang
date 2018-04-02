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

@Description {value:"ConnectorError struct represents an error occured during the HTTP client invocation"}
@Field {value:"message:  An error message explaining about the error"}
@Field {value:"cause: The error that caused ConnectorError to get thrown"}
@Field {value:"statusCode: HTTP status code"}
public struct ConnectorError {
    string message;
    error cause;
    int statusCode;
}

@Description {value:"ServerError struct represents an error occured during gRPC server excution"}
@Field {value:"message:  An error message explaining about the error"}
@Field {value:"cause: The error that caused ServerError to get thrown"}
@Field {value:"statusCode: gRPC server status code. refer: https://github
.com/grpc/grpc-java/blob/master/core/src/main/java/io/grpc/Status.java"}
public struct ServerError {
    string message;
    error cause;
    int statusCode;
}

@Description {value:"ClientError struct represents an error occured during gRPC client connector"}
@Field {value:"message:  An error message explaining about the error"}
@Field {value:"cause: The error that caused ClientError to get thrown"}
@Field {value:"statusCode: gRPC server status code. refer: https://github
.com/grpc/grpc-java/blob/master/core/src/main/java/io/grpc/Status.java"}
public struct ClientError {
    string message;
    error cause;
    int statusCode;
}
