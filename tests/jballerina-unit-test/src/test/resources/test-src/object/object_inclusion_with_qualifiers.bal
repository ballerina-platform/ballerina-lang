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

class ObjectOne {
    int i = 1;
}

class ObjectTwo {
    int j = 1;
}

class ObjectThree {
    *ObjectOne;
    *ObjectTwo;
    int i = 2;
    int j = 3;
}

type ObjectFour object {
    *ObjectOne;
    *ObjectTwo;
};

ObjectThree obThree = object ObjectOne {
    int i = 20;
    int j = 30;
};

function testReadOnlyClassInclusion() {
    var ob = object ObjectTwo {
        int j = 40;
    };
}

isolated class Foo {
    final isolated object{} i = object {};
}

client class Bar {
    int j = 2;

    remote function bar() {

    }
}

type Baz service object {
    int k;
};

isolated service class Qux {
    final isolated object{} l = object {};
}

isolated client class Quux {
    *Foo;
    *Bar;

    final isolated object{} i = object {};
    final int j = 20;
    final int k = 30;
    final isolated object{} l = object {};

    remote function bar() {

    }
}

type Quuz isolated service object {
    *Foo;
    *Baz;
    *Qux;
};

Foo foo = isolated object Foo {
    final isolated object{} i = object {};
};

object {} bar = isolated client object Bar {
    final int j = 2;

    remote function bar() {

    }
};

function testInclusion() {
    var ob = isolated service object Qux {
        final isolated object{} l = object {};
    };
}
