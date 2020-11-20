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

import ballerina/lang.'object;

int startCount = 0;
int attachCount = 0;

public function getStartAndAttachCount() returns string {
    return attachCount.toString() + "_" + startCount.toString();
}

public class ABCD {

    *'object:Listener;

    public function __start() returns error? {
        startCount += 1;
    }

    public function __gracefulStop() returns error? {}

    public function __immediateStop() returns error? {}

    public function __attach(service s, string? name = ()) returns error? {
        attachCount += 1;
    }

    public function __detach(service s) returns error? {}
}
