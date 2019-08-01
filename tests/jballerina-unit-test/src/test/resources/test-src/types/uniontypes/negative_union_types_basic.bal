// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public type ClosedFoo record {|
    string var1;
|};

public type OpenFoo record {
    string var1;
};

public type ClosedBar record {|
    string var1 = "";
|};

public type OpenBar record {
    string var1 = "";
};

public type Bar2 record {|
    int var1;
|};

public type Foo2 record {|
    string var2;
|};

function testAmbiguousAssignment() {
    OpenBar|OpenFoo x1 = {name:"John", id:12}; // Ambiguous since both are open records
    ClosedBar|ClosedFoo x2 = {var1:"John"}; // Ambiguous since closed records and both have var1 field
    ClosedBar|OpenBar x3 = {var1:12}; // Ambigous since var1 is in closed record and other is open record
    ClosedBar|OpenBar x4 = {"var2":12}; // Not Ambigous since var2 is not in the closed record
    ClosedFoo|Foo2 x5 = {var2:"John"}; // Match to Foo2
    ClosedFoo|Foo2 x6 = {var2:12}; // Match to Foo2, but type is wrong
}
