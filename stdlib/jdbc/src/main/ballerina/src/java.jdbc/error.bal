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

public const DATABASE_ERROR_REASON = "{ballerinax/java.jdbc}DatabaseError";

# Represents the properties belonging to a `DatabaseError`
#
# + message - Error message
# + sqlErrorCode - SQL error code
# + sqlState - SQL state
# + cause - Cause of the error
public type DatabaseErrorData record {|
    string message?;
    int sqlErrorCode;
    string sqlState;
    error cause?;
|};

# Represents the properties belonging to an `ApplicationError`
#
# + message - Error message
# + cause - Cause of the error
public type ApplicationErrorData record {|
    string message?;
    error cause?;
|};

# Represents an error caused by an issue related to database accessibility, erroneous queries, constraint violations,
# database resource clean-up, and other similar scenarios.
public type DatabaseError error<DATABASE_ERROR_REASON, DatabaseErrorData>;

public const APPLICATION_ERROR_REASON = "{ballerinax/java.jdbc}ApplicationError";

# Represents an error originating from application-level causes.
public type ApplicationError error<APPLICATION_ERROR_REASON, ApplicationErrorData>;

# Represents a database or application level error returned from JDBC client remote functions.
public type Error DatabaseError|ApplicationError;
