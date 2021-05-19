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

public function test() {
    int a = 10;

    // redeclared symbol
    float a = 1.23;

    // invalid RHS expr
    var x = a + 3.14;
    var p = foo();
    var q = new;
    Person person = new;
    var r = person.foo();

    // undefined symbol reference
    var y = z;

    // undefined type reference
    Foo b;

    abc d = 10;
}

class Person {
}

type abc int;
