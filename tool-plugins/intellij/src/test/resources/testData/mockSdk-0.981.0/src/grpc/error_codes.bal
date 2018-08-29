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

documentation {Indicates operation is successful.}
@final public int OK = 0;
documentation {Indicates the operation was canceled (typically by the caller).}
@final public int CANCELED = 1;
documentation {Indicates unknown error.(e.g. Status value received is unknown)}
@final public int UNKNOWN = 2;
documentation {Indicates client specified an invalid argument.}
@final public int INVALID_ARGUMENT = 3;
documentation {Indicates operation expired before completion.}
@final public int DEADLINE_EXCEEDED = 4;
documentation {Indicates some requested entity (e.g., file or directory) was not found.}
@final public int NOT_FOUND = 5;
documentation {Indicates the attempt to create an entity failed because one already exists.}
@final public int ALREADY_EXISTS = 6;
documentation {Indicates the caller does not have permission to execute the specified operation.}
@final public int PERMISSION_DENIED = 7;
documentation {Indicates some resource has been exhausted.}
@final public int RESOURCE_EXHAUSTED = 8;
documentation {Indicates operation was rejected because the system is not in a state required for the operation's
execution.}
@final public int FAILED_PRECONDITION = 9;
documentation {Indicates the operation was aborted.}
@final public int ABORTED = 10;
documentation {Indicates specified value is out of range.}
@final public int OUT_OF_RANGE = 11;
documentation {Indicates operation is not implemented or not supported/enabled in this service.}
@final public int UNIMPLEMENTED = 12;
documentation {Indicates internal errors.}
@final public int INTERNAL = 13;
documentation {Indicates the service is currently unavailable.}
@final public int UNAVAILABLE = 14;
documentation {Indicates unrecoverable data loss or corruption.}
@final public int DATA_LOSS = 15;
documentation {Indicates the request does not have valid authentication credentials for the operation.}
@final public int UNAUTHENTICATED = 16;
