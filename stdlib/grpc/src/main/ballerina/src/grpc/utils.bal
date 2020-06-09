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

function checkErrorForRetry(Error receivedError, ErrorType[] errorTypes) returns boolean {
    ErrorType receivedErrorType = getErrorType(receivedError);
    foreach var errorType in errorTypes {
        if (errorType.toString() == receivedErrorType.toString()) {
            return true;
        }
    }
    return false;
}

function getErrorType(Error err) returns ErrorType {
    if (err is CancelledError) {
        return CancelledError;
    } else if (err is UnKnownError) {
        return UnKnownError;
    } else if (err is InvalidArgumentError) {
        return InvalidArgumentError;
    } else if (err is DeadlineExceededError) {
        return DeadlineExceededError;
    } else if (err is NotFoundError) {
        return NotFoundError;
    } else if (err is AlreadyExistsError) {
        return AlreadyExistsError;
    } else if (err is PermissionDeniedError) {
        return PermissionDeniedError;
    } else if (err is UnauthenticatedError) {
        return UnauthenticatedError;
    } else if (err is ResourceExhaustedError) {
        return ResourceExhaustedError;
    } else if (err is FailedPreconditionError) {
        return FailedPreconditionError;
    } else if (err is AbortedError) {
        return AbortedError;
    } else if (err is OutOfRangeError) {
        return OutOfRangeError;
    } else if (err is UnimplementedError) {
        return UnimplementedError;
    } else if (err is InternalError) {
        return InternalError;
    } else if (err is UnavailableError) {
        return UnavailableError;
    } else if (err is DataLossError) {
        return DataLossError;
    } else {
        return ResiliencyError;
    }
}
