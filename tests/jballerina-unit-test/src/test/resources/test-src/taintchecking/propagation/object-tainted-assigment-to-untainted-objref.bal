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


type TestObject object {
    string? field;

    function __init() {
        self.field = ();
    }

// 111111
    function test2(@sensitive TestObject o) returns string {
        o.field = self.field;
        secureFunction(<string> o.field, <string> self.field);
        return <string> o.field;
    }

    function testFunction (string input) returns string {
        return input + <string>self.field;
    }
};

function secureFunction(@sensitive string secureIn, @sensitive string insecureIn) {

}

function getTaintedString() returns @tainted string {
    return "tainted-string";
}

public function main (string... args) {
    TestObject o = new();
    o.field = getTaintedString();

    TestObject o2 = new();
    var as2 = o.test2(o2);

    var assig = o.testFunction("a");
    o.field = getTaintedString();
    // todo: delete below commented from this file.
    //o.readFromDisk(); // this is also illegal given 'readFromDisk' taint o
    //o.inputParams(4, <some tainted val>) // illegal given 2nd param taint 'o'
    //o.inputParams(<tainted val>, otherstuff) // legal given 1st param does not affect gaintedness of 'o'

    //only way to make a object instance tainted is to annotate obj variable
    //@tainted Obj to = new();
    //o.field = <some tainted val>;
    //o.readFromDisk(); // these all are valid.
    //TestObject obj = new;
    //string returnValue = obj.testFunction("staticValue");
    //secureFunction(returnValue, returnValue);
}


