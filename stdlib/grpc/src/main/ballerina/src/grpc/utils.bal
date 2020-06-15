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
    if (receivedError is CancelledError) {
        foreach var errorType in errorTypes {
            if (errorType is typedesc<CancelledError>) {
                return true;
            }
        }
    } else if (receivedError is UnKnownError) {
        foreach var errorType in errorTypes {
            if (errorType is typedesc<UnKnownError>) {
                return true;
            }
        }
    } else if (receivedError is InvalidArgumentError) {
        foreach var errorType in errorTypes {
            if (errorType is typedesc<InvalidArgumentError>) {
                return true;
            }
        }
    } else if (receivedError is DeadlineExceededError) {
        foreach var errorType in errorTypes {
            if (errorType is typedesc<DeadlineExceededError>) {
                return true;
            }
        }
    } else if (receivedError is NotFoundError) {
        foreach var errorType in errorTypes {
            if (errorType is typedesc<NotFoundError>) {
                return true;
            }
        }
    } else if (receivedError is AlreadyExistsError) {
        foreach var errorType in errorTypes {
            if (errorType is typedesc<AlreadyExistsError>) {
                return true;
            }
        }
    } else if (receivedError is PermissionDeniedError) {
        foreach var errorType in errorTypes {
            if (errorType is typedesc<PermissionDeniedError>) {
                return true;
            }
        }
    } else if (receivedError is UnauthenticatedError) {
        foreach var errorType in errorTypes {
            if (errorType is typedesc<UnauthenticatedError>) {
                return true;
            }
        }
    } else if (receivedError is ResourceExhaustedError) {
        foreach var errorType in errorTypes {
            if (errorType is typedesc<ResourceExhaustedError>) {
                return true;
            }
        }
    } else if (receivedError is FailedPreconditionError) {
        foreach var errorType in errorTypes {
            if (errorType is typedesc<FailedPreconditionError>) {
                return true;
            }
        }
    } else if (receivedError is AbortedError) {
        foreach var errorType in errorTypes {
            if (errorType is typedesc<AbortedError>) {
                return true;
            }
        }
    } else if (receivedError is OutOfRangeError) {
        foreach var errorType in errorTypes {
            if (errorType is typedesc<OutOfRangeError>) {
                return true;
            }
        }
    } else if (receivedError is UnimplementedError) {
        foreach var errorType in errorTypes {
            if (errorType is typedesc<UnimplementedError>) {
                return true;
            }
        }
    } else if (receivedError is InternalError) {
        foreach var errorType in errorTypes {
            if (errorType is typedesc<InternalError>) {
                return true;
            }
        }
    } else if (receivedError is DataLossError) {
        foreach var errorType in errorTypes {
            if (errorType is typedesc<DataLossError>) {
                return true;
            }
        }
    } else if (receivedError is UnavailableError) {
        foreach var errorType in errorTypes {
            if (errorType is typedesc<UnavailableError>) {
                return true;
            }
        }
    } else {
        foreach var errorType in errorTypes {
            if ((errorType is typedesc<ResiliencyError>) || (errorType is typedesc<AllRetryAttemptsFailed>)) {
                return true;
            }
        }
    }
    return false;
}
