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

import ballerina/log;

# Represents the Cache error type with details. This will be returned if an error occurred while doing any of the cache
# operations.
public type CacheError distinct error;

# Represents Cache related errors.
public type Error CacheError;

const string LOG_LEVEL_DEBUG = "DEBUG";
const string LOG_LEVEL_ERROR = "ERROR";
type LOG_LEVEL LOG_LEVEL_DEBUG|LOG_LEVEL_ERROR;

# Prepare the `error` as a `cache:Error` after printing an log with provided log level.
#
# + message - Error message
# + err - `error` instance
# + logLevel - Log level of the error message
# + return - Prepared `Error` instance
function prepareError(string message, error? err = (), LOG_LEVEL logLevel = LOG_LEVEL_ERROR) returns Error {
    if (logLevel == LOG_LEVEL_ERROR) {
        log:printError(message, err);
    } else if (logLevel == LOG_LEVEL_DEBUG) {
        log:printDebug(message);
    }
    Error cacheError;
    if (err is error) {
        cacheError = CacheError(message, err);
    } else {
        cacheError = CacheError(message);
    }
    return cacheError;
}
