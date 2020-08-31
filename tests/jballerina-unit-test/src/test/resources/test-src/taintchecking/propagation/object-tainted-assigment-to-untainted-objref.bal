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

class TestObject {
    string? 'field;

    function init() {
        self.'field = ();
    }

    function test2(@untainted TestObject o) returns string {
        o.'field = self.'field;
        secureFunction(<string> o.'field, <string> self.'field);
        return <string> o.'field;
    }

    function testFunction (string input) returns string {
        return input + <string>self.'field;
    }

    function setField(string f) {
        self.'field = f;
    }
}

function secureFunction(@untainted string secureIn, string insecureIn) {

}

function getTaintedString() returns @tainted string {
    return "tainted-string";
}

public function main (string... args) {
    TestObject o = new();
    o.'field = getTaintedString();

    TestObject o2 = new();
    var as2 = o.test2(o2);

    TestObject o3 = new();
    o3.setField(getTaintedString());
    secureFunction(<string> o3.'field, "");

    var assig = o.testFunction("a");
    o.'field = getTaintedString();
}
