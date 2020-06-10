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

import ballerina/io;
import ballerina/log;

public function main() {
    Fruit apple = new ("Apple");

    log:printDebug("DEBUG level log");
    log:printDebug(123456);
    log:printDebug(123456.789);
    log:printDebug(true);
    log:printDebug(function() returns string {
        return io:sprintf("Name of the fruit is is %s", apple.getName());
        });
}

public type Fruit object {
    string name;
    public function init(string name) {
        self.name = name;
    }
    function getName() returns string {
        return self.name;
    }
};
