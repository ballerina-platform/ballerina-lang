// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function foo(int x, float y = 12.3, string... z) returns @a1 {desc: "string"} string => "foo";

function add(int x, int y) returns int => x + y;

function testFnTypedesc() {
    function () returns @a1 {desc: "In a typedesc"} string fn;
}

# Function Bar
# + x - Inut x 
# + y - Input y
# + return - Return Value Description 
@a1 {
    desc: "Bar"
}
function bar(@p1 int x, float y) returns int {
    return 4;
}

# Function isolated bar
# + a - Input a
@a2
public transactional isolated function barIsolated(string a, int b) {

}

service class Class1 {

    # Get name
    # + a - Input a
    # + b - Input b
    # + return - Return (a + 2)
    @a2
    remote isolated function getName(int a, string b) returns string {
        return a.toString() + b;
    }
}

public annotation p1 on parameter;

public annotation Annot a1 on function;

public annotation a2 on function;

type Annot record {
    string desc;
};
