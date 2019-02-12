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

import ballerina/test;

// A value of type stream<T> is a distributor for values of type T: 
// when a value v of type T is put into the stream, 
// a function will be called for each subscriber to the stream with v as an argument. 
// T must be a pure type.
// TODO: disallow non-pure types as stream constraint types
// https://github.com/ballerina-platform/ballerina-lang/issues/13203 
@test:Config {
    groups: ["deviation"]
}
function testStreamConstraintBroken() {
    // the following definition should fail at compile time
    stream<FooObject> s = new;
}

public type FooObject object {
    public string fooFieldOne;

    public function __init(string fooFieldOne) {
        self.fooFieldOne = fooFieldOne;
    }

    public function getFooFieldOne() returns string {
        return self.fooFieldOne;
    }
};
