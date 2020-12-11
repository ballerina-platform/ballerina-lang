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

import ballerina/java;

public type Info record {|
   // unique identifier
   byte[] xid;
   // non-zero means this transaction was a retry of
   // a previous one
   int retryNumber;
   // probably useful for timeouts and logs
   Timestamp startTime;
   // maybe useful
   Info? prevAttempt;
|};

public type Timestamp object {
    // Returns milliseconds since 1970-01-01T00:00:00Z, not including leap seconds
    public function toMillisecondsInt() returns int;
    // Returns string in ISO 8601 format
    public function toString() returns string;
};

public type CommitHandler function(Info info);
public type RollbackHandler function(Info info, error? cause, boolean willRetry);


public transactional function onCommit(CommitHandler handler) = @java:Method {
    'class: "org.ballerinalang.langlib.transaction.OnCommit",
    name: "onCommit"
} external;

public transactional function onRollback(RollbackHandler handler) = @java:Method {
    'class: "org.ballerinalang.langlib.transaction.OnRollback",
    name: "onRollback"
} external;

public transactional function info() returns Info = @java:Method {
    'class: "org.ballerinalang.langlib.transaction.Info",
    name: "info"
} external;

public transactional function setRollbackOnly(error? e) {
    if(e is error) {
      TransactionError trxError = prepareError(e.message(), e);
      wrapRollbackError(trxError);
    }
}

function wrapRollbackError(Error? e) = @java:Method {
    'class: "org.ballerinalang.langlib.transaction.WrapRollbackError",
    name: "wrapRollbackError"
} external;

public transactional function getRollbackOnly() returns boolean = @java:Method {
    'class: "org.ballerinalang.langlib.transaction.GetRollbackOnly",
    name: "getRollbackOnly"
} external;

public transactional function setData((any|error) & readonly e) = @java:Method {
    'class: "org.ballerinalang.langlib.transaction.SetData",
    name: "setData"
} external;

public transactional function getData() returns (any|error) & readonly = @java:Method {
    'class: "org.ballerinalang.langlib.transaction.GetData",
    name: "getData"
} external;

public function getInfo(byte[] xid) returns Info? = @java:Method {
    'class: "org.ballerinalang.langlib.transaction.GetInfo",
    name: "getInfo"
} external;
