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

function query(string q, typedesc<record {|int...;|}> rowType = <>) returns stream<rowType, error?> = external;

function testInvalidArgForInferTypedesc() {
    stream<OpenRecord, error?> stm = query("");
    stream<OpenRecord, error?> stm2 = query("", OpenRecord);
}

type OpenRecord record {

};

function queryWithMultipleInferTypedescs(string q, typedesc<record {|int...;|}> rowType = <>,
                                         typedesc<error> errorType = <>) returns stream<rowType, errorType?> = external;

class ClassWithMethodWithMultipleInferTypedescs {
    function queryWithMultipleInferTypedescs(typedesc<record {}> rowType = <>, typedesc<error> errorType = <>)
                returns stream<rowType, errorType?> = external;
}

stream<record {| int x; |}, error?> stm = queryWithMultipleInferTypedescs("");

ClassWithMethodWithMultipleInferTypedescs cl = new;
stream<record {| int x; |}, error?> stm2 = cl.queryWithMultipleInferTypedescs();
stream<record {| int x; |}, error?> stm3 = cl.queryWithMultipleInferTypedescs(rowType = OpenRecord);

function func1(typedesc<string[]> t = <>) returns t|int[] = external;

function func2(typedesc<any> t = <>) returns t|int[] = external;

function func3(typedesc<any|error> t = <>) returns t|MyError = external;

function func4(typedesc<[boolean, json]|string> t = <>) returns t|int[] = external;

function func5(typedesc<map<int>|map<boolean>> t = <>) returns t|record {} = external;

function func6(int x, typedesc<int> t = <>) returns t|int:Signed16|boolean = external;

function func7(typedesc<xml:Element|xml:Comment> t = <>) returns t|xml = external;

function func8(typedesc<string> t = <>) returns string:Char|t = external;

type MyError error<record {|int code;|}>;

type XmlComment xml:Comment;
type XmlElement xml:Element;

function testXml() {
    xml<xml:Element> x = xml `<foo/>`;
    xml:Comment a = getXml(XmlComment, x);
    xml<xml:Element> b = getXml(XmlComment, x);
    xml<xml:Text> c = getXml(XmlElement);
    xml<xml:Comment> d = getXml(td = XmlElement, val = x);
    xml<xml:Comment> e = getXml(td = XmlText, val = x);
}

type XmlText xml:Text;

function getXml(typedesc<xml:Element|xml:Comment> td = <>, xml<xml:Element|xml:Comment> val = xml `<foo/>`)
    returns xml<td> = external;

function testUnionTypes() {
    boolean? a = getSimpleUnion(1);
    boolean|()|float b = getSimpleUnion("hello");
    float[]|map<float[]>? e = getComplexUnion();
    map<[int, string][]>|()|[int, boolean][] f = getComplexUnion();
    boolean? g = getSimpleUnion(1, int);
    boolean|()|float h = getSimpleUnion(td = float, val = "hello");
    float|boolean? i = getSimpleUnion(1, td = string);
    string|boolean j = getSimpleUnion("hello", string);
    int[]|map<float[]>? k = getComplexUnion(int);
    map<[int, int][]>|()|[int, int][] l = getComplexUnion();
}

function getSimpleUnion(string|int val, typedesc<string|int> td = <>) returns td|boolean? = external;

function getComplexUnion(typedesc<int|[int, string]> td = <>) returns td[]|map<td[]>? = external;

function testArgCombinations() {
    string[] a = funcWithMultipleArgs(1, int, ["hello", "world"]);
    int[] b = funcWithMultipleArgs(td = string, arr = ["hello", "world"], i = 3);

    record {| string[] arr = ["hello", "world", "Ballerina"]; int i = 123; typedesc<int> td; |} rec1 = {td: int};
    string[] c = funcWithMultipleArgs(...rec1);

    record {| string[] arr = ["hello", "world"]; |} rec2 = {};
    string[] d = funcWithMultipleArgs(1234, int, ...rec2);

    [int, typedesc<string>, string[]] tup1 = [21, string, ["hello"]];
    int[] e = funcWithMultipleArgs(...tup1);

    [string[]] tup2 = [["hello"]];
    int[] f = funcWithMultipleArgs(34, string, ...tup2);

    boolean[] g = funcWithMultipleArgs(1);

    float[] h = funcWithMultipleArgs(101, arr = ["hello", "world"]);

    json[] i = funcWithMultipleArgs(arr = ["hello", "world"], i = 202);
}

function funcWithMultipleArgs(int i, typedesc<int|string> td = <>, string[] arr = []) returns td[] = external;

function testTuples() {
    [int] a = getTupleWithNoRestDesc();
    [int, string, boolean] b = getTupleWithNoRestDesc();
    [int...] c = getTupleWithNoRestDesc();

    [int] d = getTupleWithRestDesc(int);
    [int, string, boolean] e = getTupleWithRestDesc(int);
    [int...] f = getTupleWithRestDesc(int);
    [string] g = getTupleWithRestDesc(int);
    [string] h = getTupleWithRestDesc(int, string);
}

function getTupleWithNoRestDesc(typedesc td = <>) returns [int, td] = external;

function getTupleWithRestDesc(typedesc<int|string> td1, typedesc td2 = <>) returns [td1, td2...] = external;

function funcReturningUnionWithBuiltInRefType(stream<int>? strm = (), typedesc<stream<int>> td = <>)
    returns readonly|td|handle = external;

type IntStream stream<int>;

function testBuiltInRefType() {
    stream<int> strm = (<int[]> [1, 2, 3]).toStream();

    readonly|handle|stream<string> a = funcReturningUnionWithBuiltInRefType(strm);
    stream<byte> b = funcReturningUnionWithBuiltInRefType();
    stream<int>|readonly c = funcReturningUnionWithBuiltInRefType(int);
    stream<boolean>|readonly d = funcReturningUnionWithBuiltInRefType(strm, IntStream);
    readonly|handle e = funcReturningUnionWithBuiltInRefType();
}

function testUsageWithVar() {
    var x = funcReturningUnionWithBuiltInRefType(());
    var y = funcReturningUnionWithBuiltInRefType();
}

function testInvalidUsageWithNonTypeDescType1(int x = <>) {
}

function testInvalidUsageWithNonTypeDescType2(int|typedesc<int> x = <>) {
}

function getFunctionWithAnyFunctionParamType(function (function, int) x, typedesc<int> td = <>)
    returns function (function, td) = external;

function testFunctionWithAnyFunctionParamType() {
   var fn = function (function x, int y) {
   };

   function (function, boolean) y = getFunctionWithAnyFunctionParamType(fn);
}

function quux(typedesc<boolean> td = <>) returns int = external;
function bar(typedesc<boolean> td = <>) = external;
function baz(typedesc t, typedesc<boolean> td = <>) returns int|string = external;
function qux(typedesc t, typedesc<boolean> td = <>) returns int|string|t = external;

function (typedesc<boolean> td = <>) returns int fn1 = quux;
function (typedesc<boolean> td = <>) fn2 = bar;
function (typedesc t, typedesc<boolean> td = <>) returns int|string fn3 = baz;

function (typedesc t = <>, typedesc<boolean> td = <>) returns t|td fn4 = baz;

type BooleanStream stream<boolean>;

function testInvalidUsageWithCasts() {
    var a = <function (function, boolean)> getFunctionWithAnyFunctionParamType(function (function x, int y) { });
    BooleanStream|handle b = <BooleanStream> check funcReturningUnionWithBuiltInRefType(());
}
