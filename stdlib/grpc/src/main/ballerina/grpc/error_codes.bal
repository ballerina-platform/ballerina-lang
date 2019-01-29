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

//Package defines the canonical error codes used by gRPC.

# Indicates operation is successful.
public const int OK = 0;
# Indicates the operation was canceled (typically by the caller).
public const int CANCELED = 1;
# Indicates unknown error.(e.g. Status value received is unknown)
public const int UNKNOWN = 2;
# Indicates client specified an invalid argument.
public const int INVALID_ARGUMENT = 3;
# Indicates operation expired before completion.
public const int DEADLINE_EXCEEDED = 4;
# Indicates some requested entity (e.g., file or directory) was not found.
public const int NOT_FOUND = 5;
# Indicates the attempt to create an entity failed because one already exists.
public const int ALREADY_EXISTS = 6;
# Indicates the caller does not have permission to execute the specified operation.
public const int PERMISSION_DENIED = 7;
# Indicates some resource has been exhausted.
public const int RESOURCE_EXHAUSTED = 8;
# Indicates operation was rejected because the system is not in a state required for the operation's execution.
public const int FAILED_PRECONDITION = 9;
# Indicates the operation was aborted.
public const int ABORTED = 10;
# Indicates specified value is out of range.
public const int OUT_OF_RANGE = 11;
# Indicates operation is not implemented or not supported/enabled in this service.
public const int UNIMPLEMENTED = 12;
# Indicates internal errors.
public const int INTERNAL = 13;
# Indicates the service is currently unavailable.
public const int UNAVAILABLE = 14;
# Indicates unrecoverable data loss or corruption.
public const int DATA_LOSS = 15;
# Indicates the request does not have valid authentication credentials for the operation.
public const int UNAUTHENTICATED = 16;
