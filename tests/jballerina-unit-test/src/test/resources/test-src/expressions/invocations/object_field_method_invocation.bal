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

class F {
    function () returns int f = function() returns int => 1;
    function () returns int fooDel = foo;
    //function () returns int selfFooDel;

    function foo() returns int {
        return 333;
    }

// todo: uncomment after fixing https://github.com/ballerina-platform/ballerina-lang/issues/28457
//  function init() {
//     self.selfFooDel = self.foo;
// }
}

type T object {
    function () returns int f;
};

class D {
    function () returns int foo;

    function init() {
        F f = new F();
        self.foo = f.foo;
    }
}

function foo() returns int {
    return 222;
}

isolated class CoreState {
    private int i = 0;

    isolated function inc() {
        lock {
            self.i = self.i + 1;
        }
    }

    isolated function dec() {
        lock {
            self.i = self.i - 1;
        }
    }

    isolated function get() returns int {
        lock {
            return self.i;
        }
    }

    isolated function init(int i) {
        self.i = i;
    }
}

isolated class Proxy {
    private CoreState state;

    final isolated function () inc;
    final isolated function () dec;
    final isolated function () returns int get;

    isolated function init(int i) {
        self.state = new (i);
        self.inc = self.state.inc;
        self.dec = self.state.dec;
        self.get = self.state.get;
    }
}

public function testInvocationOfObjectField() {
    F f = new ();
    assertEquals(1, f.f());
    assertEquals(foo(), f.fooDel());
    //assertEquals(333, f.selfFooDel());

    D d = new ();
    assertEquals(f.foo(), d.foo());

    T t = new F();
    assertEquals(1, t.f());
}
public isolated function testInvocationOfObjectFieldWithMutableState() {
    Proxy proxy = new (10);
    proxy.inc();
    assertEquals(11, proxy.get());
    proxy.dec();
    assertEquals(10, proxy.get());

    proxy.dec();
    proxy.dec();
    proxy.dec();
    proxy.dec();
    proxy.dec();
    assertEquals(5, proxy.get());
}

isolated function assertEquals(anydata|error expected, anydata|error actual) {
    if (actual is anydata && expected is anydata && actual == expected) {
        return;
    } else if (actual === expected) {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error("expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
