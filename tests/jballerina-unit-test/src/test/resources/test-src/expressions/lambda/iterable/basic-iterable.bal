import ballerina/io;
import ballerina/'lang\.int as ints;
import ballerina/'lang\.float as floats;

int add = 0;
int index = 0;

function testInt1() returns [int, int, int, int, int] {
    add = 0;
    int[] fa = [-5, 2, 4, 5, 7, -8, -3, 2];
    fa.forEach(function (int i) { add = add + i;});
    int fadd1 = add;
    int count = fa.length();
    int max = ints:max(0, ...fa);
    int min = ints:min(0, ...fa);
    int sum = ints:sum(...fa);
    return [fadd1, count, max, min, sum];
}

function testInt2() returns [int, int, int, int] {
    int[] fa = [-5, 2, 4, 5, 7, -8, -3, 2];
    int count = fa.filter(function (int i) returns boolean {
                    return i >= 0;
                }).length();
    int max = ints:max(0, ...fa.filter(filterIntNegative));
    int min = ints:min(fa.filter(filterIntNegative)[0], ...fa.filter(filterIntNegative));
    int sum = ints:sum(...fa.filter(filterIntNegative));
    return [count, max, min, sum];
}

function filterIntNegative(int i) returns boolean {
    return i >= 0;
}

float fadd = 0;
function testFloat1() returns [float, int, float, float, float]{
    fadd = 0.0;
    float[] fa = [1.1, 2.2, -3.3, 4.4, 5.5];
    fa.forEach(function (float i) { fadd = fadd + i;});
    float fsum = fadd;
    int count = fa.length();
    float max = floats:max(...fa);
    float min = floats:min(...fa);
    float sum = floats:sum(...fa);
    return [fsum, count, max, min, sum];
}

function testFloat2() returns [int, float, float, float]{
    float[] fa = [1.1, 2.2, -3.3, 4.4, 5.5];
    int count = fa.filter(filterFloatNegative).length();
    float max = floats:max(...fa.filter(filterFloatNegative));
    float min = floats:min(...fa.filter(filterFloatNegative));
    float sum = floats:sum(...fa.filter(filterFloatNegative));
    return [count, max, min, sum];
}

function filterFloatNegative(float i) returns boolean {
    return i >= 0;
}

string output = "";

function testBasicArray1(string[] values) returns string {
    output = "";
    values.'map(mapString).forEach(concat);
    return output.trim();
}

function mapString(string x) returns [string, string] {
    string up = x.toUpperAscii();
    string lower = x.toLowerAscii();
    return [up, lower];
}

function concat([string, string] value) {
    var [up, lower] = value;
    output = output + " " + up + ":" + lower;
}

function testBasicArray2(string[] values) returns string {
    output = "";

    index = 0;
    values.'map(function (string s) returns string {
                    var value = index.toString() + s;
                    index += 1;
                    return value;
               })
    .forEach(function (string s) {
                 output = output + s + " ";
             });
    return output.trim();
}
// Commenting out due to lack of map to string langlib function
//function testBasicMap1() returns [int, string[]] {
//    map<string> m = {a:"A", b:"B", c:"C", d:"D", e:"E"};
//    int count = m.length();
//    string[] values = m.entries().'map(function ([string, string] value) returns string {
//                                var [k, v] = value;
//                                return k.toLowerAscii();
//                            })
//                      .filter(function (string v) returns boolean {
//                                  if (v == "a" || v == "e") {
//                                      return true;
//                                  }
//                                  return false; });
//    return [count, values];
//}
//
//function testBasicMap2() returns string[] {
//    map<string> m = {a:"A", b:"B", c:"C", d:"D", e:"E"};
//    string[] values = m.entries()
//                      .'map(mapToTuple)
//                      .filter(function ([string, string] v) returns boolean {
//                                  var [k, t] = v;
//                                  if (k == "a" || k == "e") {
//                                      return true;
//                                  }
//                                  return false; })
//                      .'map(concatString);
//    return values;
//}

function mapToTuple([string, string] tuple) returns [string, string] {
    var [key, value] = tuple;
    return [key, value];
}

function concatString([string, string] v) returns string {
    var [v1, v2] = v;
    return v1 + v2;
}

//function xmlTest() returns [int, int, map<any>] {
//    xml xdata = xml `<p:person xmlns:p="foo" xmlns:q="bar">
//        <p:name>bob</p:name>
//        <p:address>
//            <p:city>NY</p:city>
//            <q:country>US</q:country>
//        </p:address>
//        <q:ID>1131313</q:ID>
//    </p:person>`;
//    int nodeCount = xdata.*.length();
//    int elementCount = xdata.*.elements().length();
//
//    index = -1;
//    map<xml> m = xdata.*.elements()[1].*.elements()
//                 .'map(function (xml|string x) returns [string, xml] {
//                            index += 1;
//                            if x is xml {
//                                return [string.convert(index), x];
//                            }
//                            return ["", xml ` `];
//                      });
//    return [nodeCount, elementCount, m];
//}

type person record {
    string name;
    int age;
};

function structTest() returns [int, string[]] {
    person bob = {name:"bob", age:30};
    person tom = {name:"tom", age:20};
    person sam = {name:"sam", age:24};
    person[] p = [bob, tom, sam];
    int count = p.filter(isBellow25).length();
    string[] names = p.'map(getName);
    return [count, names];
}

function getName(person p) returns string {
    return p.name;
}

function isBellow25(person p) returns boolean {
    return p.age < 25;
}

function testIgnoredValue() returns (string) {
    output = "";
    string[] s = ["abc", "cd", "pqr"];
    s = s.filter(function (string ss) returns boolean {return ss.length() == 3;})
    .'map(function (string ss) returns string {
            output = output + " " + ss;
            return (ss + ss);
             });
    return output.trim();
}

function appendAny(any a) {
    var s = <string>a;
    output = s;
}

function testInExpression() returns [string, int] {
    output = "";
    string[] s = ["abc", "cd", "pqr"];
    float[] r = [1.1, -2.2, 3.3, 4.4];
    appendAny("total count " + s.filter(function (string ss) returns boolean {return ss.length() == 3;}).length());
    int i = s.length() + r.length();
    return [output, i];
}

function testInFunctionInvocation() returns int {
    map<string> m = {a:"abc", b:"cd", c:"pqr"};
    return doubleInt(m.entries().filter(function ([string, string] value) returns boolean {
                                  var [k, v] = value;
                                  int i = v.length();
                                  return i == 3;})
                     .length());
}

function doubleInt(int i) returns int {
    return i * 2;
}

function testInStatement() returns int {
    map<string> m = {a:"abc", b:"cd", c:"pqr"};
    if (5 > m.entries().filter(function ([string, string] value) returns boolean {
                         var [k, v] = value;
                         return v.length() == 3;})
            .length()) {
        return 10;
    }
    return 0;
}

function testIterableOutputPrint() returns [any, any, any, any] {
    map<string> m = {a:"abc", b:"cd", c:"pqr"};
    any count = m.length();
    foo(count);
    foo(m.length());

    int[] a = [-5, 2, 4, 5, 7, -8, -3, 2];

    foo(a[0].min(...a));
    any c = a[0].min(...a);
    foo(c);

    foo(a[0].max(...a));
    any d = a[0].max(...a);
    foo(d);

    foo(ints:sum(...a));
    any e = ints:sum(...a);
    foo(e);

    return [count, c, d, e];
}

function foo(any a) {
    //do nothing
}

// Commenting out due to missing map to list support
//function testIterableReturnLambda() returns (function (int) returns boolean)?[] {
//
//    map<string> words = { a: "ant", b: "bear", c: "tiger"};
//
//    (function (int) returns boolean)?[] lambdas = words.entries().'map(function ([string, string] input) returns
//        (function (int) returns boolean) {
//        return function (int param) returns boolean {
//            return true;
//        };
//    });
//
//    return lambdas;
//}
