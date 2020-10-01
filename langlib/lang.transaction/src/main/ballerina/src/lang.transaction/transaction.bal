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
   int startTime;
   // maybe useful
   Info? prevAttempt;
|};

# This file contains default retry manager to be used with retry statement.

public type RetriableError error;
//todo use distinct when grammer allowes
//public type RetriableError distinct error;

public type RetryManager object {
 public function shouldRetry(error? e) returns boolean;
};

public class DefaultRetryManager {
    private int count;
    public function init(int count = 3) {
        self.count = count;
    }
    public function shouldRetry(error? e) returns boolean {
        if e is RetriableError && self.count >  0 {
          self.count -= 1;
          return true;
        } else {
           return false;
        }
    }
}

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
