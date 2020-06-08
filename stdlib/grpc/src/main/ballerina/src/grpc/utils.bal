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

function checkErrorForRetry(Error err, ErrorType[] errors) returns boolean {
    var typeOfError = getErrorType(err);
    if (typeOfError is ErrorType) {
        foreach var errorType in errors {
            if (errorType == typeOfError) {
                return true;
            }
        }
    }
    return false;
}

function getErrorType(Error err) returns ErrorType? {
    if (err is CancelledError) {
        return CANCELLED_ERROR;
    } else if (err is UnKnownError) {
        return UNKNOWN_ERROR;
    } else if (err is InvalidArgumentError) {
        return INVALID_ARGUMENT_ERROR;
    } else if (err is DeadlineExceededError) {
        return DEADLINE_EXCEEDED_ERROR;
    } else if (err is NotFoundError) {
        return NOT_FOUND_ERROR;
    } else if (err is AlreadyExistsError) {
        return ALREADY_EXISTS_ERROR;
    } else if (err is PermissionDeniedError) {
        return PERMISSION_DENIED_ERROR;
    } else if (err is UnauthenticatedError) {
        return UNAUTHENTICATED_ERROR;
    } else if (err is ResourceExhaustedError) {
        return RESOURCE_EXHAUSTED_ERROR;
    } else if (err is FailedPreconditionError) {
        return FAILED_PRECONDITION_ERROR;
    } else if (err is AbortedError) {
        return ABORTED_ERROR;
    } else if (err is OutOfRangeError) {
        return OUT_OF_RANGE_ERROR;
    } else if (err is UnimplementedError) {
        return UNIMPLEMENTED_ERROR;
    } else if (err is InternalError) {
        return INTERNAL_ERROR;
    } else if (err is UnavailableError) {
        return UNAVAILABLE_ERROR;
    } else if (err is DataLossError) {
        return DATA_LOSS_ERROR;
    } else {
        return ALL_RETRY_ATTEMPTS_FAILED;
    }
}
