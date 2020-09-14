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

client class TestEP {
    remote function action1(string s, int i) returns boolean {
        if (i > 5) {
            return true;
        }
        return false;
    }

    remote function action2(string s, boolean b) returns int {
        return 10;
    }

    function func1(string s) {

    }

    function func2(string s) returns int {
        return 5;
    }
}

function test1(int i) returns boolean{
    TestEP z;
    TestEP x = new;
    TestEP y = new;
    x.func1("abc");
    boolean b = x->action1("test1", i);
    return b;
}

function test2() returns int {
    TestEP x = new;
    return x.func2("test");
}

TestEP globEP = new;

class TestObject {
    string s = "abc";
    TestEP x = new;

    function test() returns boolean {
        _ = globEP->action1("test1", 10);
        boolean b = self.x->action1("test1", 10);
        return b;
    }
}

function test3() returns boolean {
    TestObject x = new;
    return x.test();
}

function clientObjectDeclaredInLoop() returns int {
    foreach var index in 1...3 {
        TestEP x = new;
        if (4 - 2 == 2) {
            return x->action2("0", false);
        }
    }
    return 0;
}

function clientObjectDeclaredInIfStatement() returns int {
    if (4 - 2 == 2) {
        TestEP x = new;
        return x->action2("0", false);
    }
    return 0;
}

function clientObjectDeclaredInIfStatementElseBlock() returns int {
    if (4 - 2 == 0) {
        return 0;
    } else {
        int i = 0;
        int j = 100;
        int k = i + j;
        TestEP x = new;
        return x->action2("0", false);
    }
}
