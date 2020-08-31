// Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.





// remote modifier not allowed in non-object attached functions
remote function test1(string value) {
}

// remote modifier not allowed in non-object attached functions
public remote function test2(string value) {
}

// remote modifier not allowed in non-object attached functions
public remote function test3(string value) returns int = external;


client class Foo {

    function abc (string value) returns int {
        return 10;
    }

    remote function pqr(string value) returns boolean {
        return false;
    }

    remote function xyz (string value) returns float {
        return 10.0;
    }
}

function testFunc1() {
    Foo x = new;
    // invalid remote method call '.pqr()': use '->pqr()' for remote method calls
    var y = x.pqr("test");
    // invalid method call '->abc()': '->' can only be used with remote methods
    var z = x->abc("test");
}


function testFunc2() {
    // Testing other type invocation.
    XXX x = new;
    // invalid remote function invocation, expected an endpoint
    var y = x->foo();

    map<any> m = {};
    // invalid remote function invocation, expected an endpoint
    _ = m->keys();

    Bar b = new;
    // invalid remote function invocation, expected an endpoint
    _ = b->action1("value");
}

class Bar {
   function action1 (string x) returns int {
       return 10;
   }
}

// valid.
Foo gep = new;

function testFunc5() {
    var y = gep->pqr("test");

    // invalid remote method call '.pqr()': use '->pqr()' for remote method calls
    var z = gep.pqr("test");
}


function testFunc6(Foo ep, string b) {
    var y = ep->pqr("test");

    // invalid remote method call '.pqr()': use '->pqr()' for remote method calls
    var z = ep.pqr("test");
}

function testFunc9 (string s) returns boolean{
    Foo ep;
    string a = "abc";
    // Uninitilized variable
    return ep->pqr("test");
}

function testFunc10 (string s) {
    Foo ep;
    string a = "abc";
    // Uninitilized variable
    var y = ep->pqr("test");
}

class Baz {
    //a remote function in a non client object
    remote function action1(string s) returns int {
        return 10;
    }

    function nonAction(float f) returns string {
        return "done";
    }

    //a remote function in a non client object
    remote function action2(int i) {
    }
}

class dummy {

    Foo ff = new;

    function nonAction1(float f) returns string {
        _ = self.ff->pqr("test");
        return "done";
    }

}
