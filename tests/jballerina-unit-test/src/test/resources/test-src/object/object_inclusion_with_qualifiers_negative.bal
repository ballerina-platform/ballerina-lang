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
    final int i = 1;
}

readonly class ObjectTwo {
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

isolated class Foo {
    final int i = 1;
}

client class Bar {
    int j = 2;

    remote function bar() {

    }
}

type Baz service object {
    int k;
};

isolated client class Qux {
    final isolated object{} l = object {};
}

class Quux {
    *Foo;
    *Bar;
    *Baz;
    *Qux;

    int i = 10;
    int j = 20;
    final int k = 30;
    final isolated object{} l = object {};

    remote function bar() {

    }
}

type Quuz object {
    *Foo;
    *Bar;
    *Baz;
    *Qux;
};

Foo foo = object Foo {
    final int i = 20;
};

object {} bar = object Bar {
    int j = 2;

    remote function bar() {

    }
};

function testInvalidInclusion() {
    var ob = object Qux {
        final isolated object{} l = object {};
    };
}

readonly class Corge {
    int x;
}

object {
    int x;
} invalidReadOnlyReferenceInObjectConstructorExpression = isolated object Foo {
    int x = 1;
    private stream<int> y;

    function init() {
        int[] arr = [1, 2];
        self.y = arr.toStream();
    }
};

function testInvalidReadOnlyReferenceInObjectConstructorExpression() {
    object {
        int x;
    } ob = object Corge {
        int x = 1;
        stream<int> y;
        future<()> z;

        function init() {
            int[] arr = [1, 2];
            self.y = arr.toStream();
            self.z = start testInvalidReadOnlyReferenceInObjectConstructorExpression();
        }
    };
}
