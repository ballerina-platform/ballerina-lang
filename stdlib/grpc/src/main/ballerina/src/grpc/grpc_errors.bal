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

# Represents the operation canceled(typically by the caller) error.
public type CancelledError distinct error;

# Represents unknown error.(e.g. Status value received is unknown)
public type UnKnownError distinct error;

# Represents client specified an invalid argument error.
public type InvalidArgumentError distinct error;

# Represents operation expired before completion error.
public type DeadlineExceededError distinct error;

# Represents requested entity (e.g., file or directory) not found error.
public type NotFoundError distinct error;

# Represents error occur when attempt to create an entity which already exists.
public type AlreadyExistsError distinct error;

# Represents error occur when the caller does not have permission to execute the specified operation.
public type PermissionDeniedError distinct error;

# Represents error occur when the request does not have valid authentication credentials for the operation.
public type UnauthenticatedError distinct error;

# Represents error occur when the resource is exhausted.
public type ResourceExhaustedError distinct error;

# Represents error occur when operation is rejected because the system is not in a state required for the operation's execution.
public type FailedPreconditionError distinct error;

# Represents error occur when operation is aborted.
public type AbortedError distinct error;

# Represents error occur when specified value is out of range.
public type OutOfRangeError distinct error;

# Represents error occur when operation is not implemented or not supported/enabled in this service.
public type UnimplementedError distinct error;

# Represents internal error.
public type InternalError distinct error;

# Represents error occur when the service is currently unavailable.
public type UnavailableError distinct error;

# Represents unrecoverable data loss or corruption erros.
public type DataLossError distinct error;

# Represents error scenario where the maximum retry attempts are done and still received an error.
public type AllRetryAttemptsFailed distinct error;

# Represents all the resiliency-related errors.
public type ResiliencyError AllRetryAttemptsFailed;

# Represents an error when calling next when the stream has closed.
public type StreamClosedError distinct error;

# Represents an error when reaching the end of the client stream.
public type EOS distinct error;

# Represents gRPC related errors.
public type Error CancelledError | UnKnownError | InvalidArgumentError | DeadlineExceededError | NotFoundError
| AlreadyExistsError | PermissionDeniedError | UnauthenticatedError | ResourceExhaustedError | FailedPreconditionError
| AbortedError | OutOfRangeError | UnimplementedError | InternalError | UnavailableError | DataLossError
| ResiliencyError | StreamClosedError | EOS;

# Represents gRPC related error types.
public type ErrorType typedesc<Error>;
