// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function testBasic() returns (string, int, float) {
    (string, int, float) (s, i, f) = ("Fooo", 4, 6.7);
    return (s, i, f);
}

function testBasicRecursive1() returns (string, int, float) {
    ((string, int), float) ((s, i), f) = (("Fooo", 4), 6.7);
    return (s, i, f);
}

function testBasicRecursive2() returns (string, int, boolean, float) {
    ((string, int), (boolean, float)) ((s, i),(b, f)) = (("Fooo", 34), (true, 6.7));
    return (s, i, b, f);
}

function testComplexRecursive() returns (string, int, boolean, byte, float, int){
    ((string, (int, (boolean, byte))), (float, int)) ((s, (i1, (b, y))), (f, i2)) = (("Bal", (3, (true, 34))), (5.6, 45));
    return (s, i1, b, y, f, i2);
}

function testRecursiveWithExpression() returns (string, int, boolean, byte, float, int){
    ((string, (int, (boolean, byte))), (float, int)) a = (("Bal", (3, (true, 34))), (5.6, 45));
    ((string, (int, (boolean, byte))), (float, int)) ((s, (i1, (b, y))), (f, i2)) = a;
    return (s, i1, b, y, f, i2);
}

function testTupleBindingWithRecordsAndObjects() returns (string, int, int, boolean, string, float, byte, boolean, int) {
    Foo foo = {name:"Test", age:23};
    Bar bar = {id:34, flag:true};
    FooObj fooObj = new ("Fooo", 3.7, 23);
    BarObj barObj = new (true, 56);
    ((Foo, (BarObj, FooObj)), Bar) ((f, (bo, fo)), b) = ((foo, (barObj, fooObj)), bar);
    return (f.name, f.age, b.id, b.flag, fo.s, fo.f, fo.b, bo.b, bo.i);
}

type Foo record {
    string name;
    int age;
};

type Bar record {
    int id;
    boolean flag;
};

type FooObj object {
    public string s;
    public float f;
    public byte b;
    public new(s, f, b){}
};

type BarObj object {
    public boolean b;
    public int i;
    public new(b, i){}
};
