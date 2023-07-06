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

type ObjectOne readonly & object {
    int i;
};

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
    *ObjectOne; // OK
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
    *Foo; // OK
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
    *Foo; // OK
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
} invalidReadOnlyReferenceInObjectConstructorExpression = isolated object Corge {
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

public type Timestamp readonly & object {
    public function toString() returns string;
};

class InvalidNonReadOnlyClassWithInvalidReadOnlyIntersectionInclusion {
    *Timestamp;

    public function toString() returns string => "";
}

type InvalidObjectTypeDescriptorWithInvalidReadOnlyIntersectionInclusion object {
    *Timestamp;

    public function toInt() returns int;
};

public type Type object {
    public int[] vals;
    string name;
    int age;

    function foo(float ratio, int months) returns float;
};

public class Class {
    *Type;
    int[] vals = [];
    public string name = "";
    private int age = 23;
    public float x;
    boolean y;

    public function foo(float ratio, int months) returns float {
        return 1000.00 * ratio * <float>months;
    }
}

type Obj0 distinct object {
    int a;
    public float b;
    string c;
};

type Obj1 distinct object {
    *Obj0;
    public int a;
    float b;
};

type Obj2 distinct object {
    *Class;
    float x;
    public boolean y;
};

public class Class2 {
    int l;
    float m;
    public boolean n;

    public function foo(float ratio, int months) returns float => 3000.00 * ratio * <float>months;
}

public class Class3 {
    *Class2;
    private int l = 23;
    public float m;
    boolean n;

    private function foo(float ratio, int months) returns float {
        return 1000.00 * ratio * <float>months;
    }
}

isolated client class Class4 {
    private int q = 1;
}

isolated client class Class5 {
    *Class4;
    final int q = 1;
}
