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

# Reference: https://github.com/grpc/grpc/blob/master/doc/statuscodes.md
#
# Indicates the operation was canceled (typically by the caller).
public const CANCELLED_ERROR = "{ballerina/grpc}CancelledError";
public type CancelledError error<CANCELLED_ERROR, Detail>;

# Indicates unknown error.(e.g. Status value received is unknown)
public const UNKNOWN_ERROR = "{ballerina/grpc}UnKnownError";
public type UnKnownError error<UNKNOWN_ERROR, Detail>;

# Indicates client specified an invalid argument.
public const INVALID_ARGUMENT_ERROR = "{ballerina/grpc}InvalidArgumentError";
public type InvalidArgumentError error<INVALID_ARGUMENT_ERROR, Detail>;

# Indicates operation expired before completion.
public const DEADLINE_EXCEEDED_ERROR = "{ballerina/grpc}DeadlineExceededError";
public type DeadlineExceededError error<DEADLINE_EXCEEDED_ERROR, Detail>;

# Indicates some requested entity (e.g., file or directory) was not found.
public const NOT_FOUND_ERROR = "{ballerina/grpc}NotFoundError";
public type NotFoundError error<NOT_FOUND_ERROR, Detail>;

# Indicates the attempt to create an entity failed because one already exists.
public const ALREADY_EXISTS_ERROR = "{ballerina/grpc}AleadyExistsError";
public type AleadyExistsError error<ALREADY_EXISTS_ERROR, Detail>;

# Indicates the caller does not have permission to execute the specified operation.
public const PERMISSION_DENIED_ERROR = "{ballerina/grpc}PermissionDeniedError";
public type PermissionDeniedError error<PERMISSION_DENIED_ERROR, Detail>;

# Indicates the request does not have valid authentication credentials for the operation.
public const UNAUTHENTICATED_ERROR = "{ballerina/grpc}UnauthenticatedError";
public type UnauthenticatedError error<UNAUTHENTICATED_ERROR, Detail>;

# Indicates some resource has been exhausted.
public const RESOURCE_EXHAUSTED_ERROR = "{ballerina/grpc}ResourceExhaustedError";
public type ResourceExhaustedError error<RESOURCE_EXHAUSTED_ERROR, Detail>;

# Indicates operation was rejected because the system is not in a state required for the operation's execution.
public const FAILED_PRECONDITION_ERROR = "{ballerina/grpc}FailedPreconditionError";
public type FailedPreconditionError error<FAILED_PRECONDITION_ERROR, Detail>;

# Indicates the operation was aborted.
public const ABORTED_ERROR = "{ballerina/grpc}AbortedError";
public type AbortedError error<ABORTED_ERROR, Detail>;

# Indicates specified value is out of range.
public const OUT_OF_RANGE_ERROR = "{ballerina/grpc}OutOfRangeError";
public type OutOfRangeError error<OUT_OF_RANGE_ERROR, Detail>;

# Indicates operation is not implemented or not supported/enabled in this service.
public const UNIMPLEMENTED_ERROR = "{ballerina/grpc}UnimplementedError";
public type UnimplementedError error<UNIMPLEMENTED_ERROR, Detail>;

# Indicates internal errors.
public const INTERNAL_ERROR = "{ballerina/grpc}InternalError";
public type InternalError error<INTERNAL_ERROR, Detail>;

# Indicates the service is currently unavailable.
public const UNAVAILABLE_ERROR = "{ballerina/grpc}UnavailableError";
public type UnavailableError error<UNAVAILABLE_ERROR, Detail>;

# Indicates unrecoverable data loss or corruption.
public const DATA_LOSS_ERROR = "{ballerina/grpc}DataLossError";
public type DataLossError error<DATA_LOSS_ERROR, Detail>;

public type ErrorType CANCELLED_ERROR | UNKNOWN_ERROR | INVALID_ARGUMENT_ERROR | DEADLINE_EXCEEDED_ERROR
| NOT_FOUND_ERROR | ALREADY_EXISTS_ERROR | PERMISSION_DENIED_ERROR | UNAUTHENTICATED_ERROR | RESOURCE_EXHAUSTED_ERROR
| FAILED_PRECONDITION_ERROR | ABORTED_ERROR | OUT_OF_RANGE_ERROR | UNIMPLEMENTED_ERROR |
INTERNAL_ERROR|UNAVAILABLE_ERROR | DATA_LOSS_ERROR;


public type Error CancelledError | UnKnownError | InvalidArgumentError | DeadlineExceededError | NotFoundError
| AleadyExistsError | PermissionDeniedError | UnauthenticatedError | ResourceExhaustedError | FailedPreconditionError
| AbortedError | OutOfRangeError | UnimplementedError | InternalError | UnavailableError | DataLossError;


# Prepare the `error` as a `Error`.
#
# + errorType - the error type.
# + message - the error message.
# + other - the `error` instance.
# + return - prepared `Error` instance.
public function prepareError(ErrorType errorType, string message, error other) returns Error {
    error err = error(errorType, message = message, cause = other);
    return <Error> err;
}
