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

int x = 1;

class Foo {
    int i = x; // error, if no `init`, needs to be `isolated`
}

class Bar {
    int j = x; // error, if the `init` is isolated, needs to be `isolated`

    isolated function init() {

    }
}

class Baz {
    int k = x; // OK, `init` is not `isolated`

    function init() {
        
    }
}

isolated function testObjectDefaultIsolationNegative() {
    Foo f = new; // OK

    Bar br = new; // OK

    Baz bz = new; // error

    Foo f2 = object { // OK
        int i = x; // error
    };

    Bar br2 = object { // OK
        int j = x; // error

        isolated function init() {

        }
    };

    Baz bz2 = object { // error
        int k = x; // OK

        function init() {
            
        }
    };
}

class InvalidDefaults {
    json j = nonIsolated();
    object {
        json j;
        object {
            boolean b;
        } innerOb;
    } ob = object {
        json j;
        object {
            boolean b;
        } innerOb = object {
            boolean b = true;
        };

        function init() {
            self.j = true;
        }
    };
}

function nonIsolated() returns json => 1;
