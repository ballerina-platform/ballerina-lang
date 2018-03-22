int add;

function testInt1() returns (int, int, int, int, int, float){
    add = 0;
    int[] fa = [-5, 2, 4, 5, 7, -8, -3 , 2];
    fa.foreach(function (int i){ add = add + i;});
    int fadd = add;
    int count = fa.count();
    int max = fa.max();
    int min = fa.min();
    int sum = fa.sum();
    float avg = fa.average();
    return (fadd, count, max, min, sum, avg);
}

function testInt2() returns (int, int, int, int, float){
    int[] fa = [-5, 2, 4, 5, 7, -8, -3 , 2];
    int count = fa.filter(filterIntNegative).count();
    int max = fa.filter(filterIntNegative).max();
    int min = fa.filter(filterIntNegative).min();
    int sum = fa.filter(filterIntNegative).sum();
    float avg = fa.filter(filterIntNegative).average();
    return (count, max, min, sum, avg);
}

function filterIntNegative(int i) returns (boolean){
    return i >= 0;
}

function testFloat1() returns (int, int, float, float, float, float){
    add = 0;
    float[] fa = [1.1, 2.2, -3.3, 4.4, 5.5];
    fa.foreach(function (int i){ add = add + i;});
    int fcount = add;
    int count = fa.count();
    float max = fa.max();
    float min = fa.min();
    float sum = fa.sum();
    float avg = fa.average();
    return (fcount, count, max, min, sum, avg);
}

function testFloat2() returns (int, float, float, float, float){
    float[] fa = [1.1, 2.2, -3.3, 4.4, 5.5];
    int count = fa.filter(filterFloatNegative).count();
    float max = fa.filter(filterFloatNegative).max();
    float min = fa.filter(filterFloatNegative).min();
    float sum = fa.filter(filterFloatNegative).sum();
    float avg = fa.filter(filterFloatNegative).average();
    return (count, max, min, sum, avg);
}

function filterFloatNegative(float i) returns (boolean){
    return i >= 0;
}

string output;

function testBasicArray1 (string[] values) returns (string) {
    output = "";
    values.map(mapString).foreach(concat);
    return output.trim();
}

function mapString (string x) returns (string, string) {
    string up = x.toUpperCase();
    string lower = x.toLowerCase();
    return (up, lower);
}

function concat (string up, string lower) {
    output = output + " " + up + ":" + lower;
}

function testBasicArray2 (string[] values) returns (string) {
    output = "";
    values.map(function (int a, string b) returns (string) {
                    string s = a + b;
                    return s;
                })
               .foreach (function (string s){
                    output = output + s + " ";
                });
    return output.trim();
}

function testBasicMap1 () returns (int, string[]) {
    map m = {a:"A", b:"B", c:"C", d:"D", e:"E"};
    int count = m.count();
    string[] values = m.map(function (any value) returns (string) {
                    var v =? <string>value;
                    return v;
                })
                .filter(function (string v) returns (boolean) {
                       if (v == "A" || v == "E") {
                           return true;
                       }
                       return false; });
    return (count, values);
}

function testBasicMap2 () returns (string[]) {
    map m = {a:"A", b:"B", c:"C", d:"D", e:"E"};
    string[] values = m.map(mapAnyToString)
                .filter(function (string k, string v) returns (boolean) {
                       if (k == "a" || k == "e") {
                           return true;
                       }
                       return false; })
                .map(concatString);
    return values;
}

function mapAnyToString (string key, any value) returns (string, string) {
    var v =? <string>value;
    return (key, v);
}

function concatString (string v1, string v2) returns (string) {
    return v1 + v2;
}

json j1 = {name:"bob", age:10, pass:true, subjects:[{subject:"maths", marks:75}, {subject:"English", marks:85}]};
function jsonTest() returns (string, string[], int, any, string[]){
    output = "";
    j1.foreach(function (json j){
        output = output + j.toString();
    });

    string[] sa = j1.map(function (json j) returns (string){
        return j.toString();
    })
    .filter(function (string s) returns (boolean){
        return s == "bob";
    });

    int i = j1.count();

    var ja =? <json[]> j1.subjects;
    string[] result = ja.map(function (int i, json j) returns (string){
        return i + "->" + j.toString();
    });

    return (output, sa, i, j1.count(), result);
}

xml xdata = xml `<p:person xmlns:p="foo" xmlns:q="bar">
        <p:name>bob</p:name>
        <p:address>
            <p:city>NY</p:city>
            <q:country>US</q:country>
        </p:address>
        <q:ID>1131313</q:ID>
    </p:person>`;

function xmlTest() returns (int, any, map){
    int nodeCount = xdata.children().count();
    any elementCount = xdata.children().elements().count();
    map m = xdata.children().elements()[1].children().elements().map(function (int i, xml x) returns (string, xml){
        return (<string>i, x);
    });
    return (nodeCount, elementCount, m);
}

struct person {
    string name;
    int age;
}

function structTest() returns (int, string[]){
    person bob = { name: "bob", age: 30};
    person tom = { name: "tom", age: 20};
    person sam = { name: "sam", age: 24};
    person[] p = [bob, tom, sam];
    int count = p.filter(isBellow25).count();
    string[] names = p.map(getName);
    return (count, names);
}

function getName(person p) returns (string){
    return p.name;
}

function isBellow25(person p) returns (boolean){
    return p.age < 25;
}

function testIgnoredValue() returns (string){
    output = "";
    string[] s = ["abc", "cd", "pqr"];
    _ = s.filter(function(string s) returns (boolean){return lengthof s == 3;})
            .map(function(string s) returns (string){
                     output = output + " " + s;
                     return s + s;
                     });
    return output.trim();
}

function appendAny(any a){
    var s =? <string> a;
    output = s;
}

function testInExpression() returns (string, int){
    output = "";
    string[] s = ["abc", "cd", "pqr"];
    float[] r = [1.1, -2.2, 3.3, 4.4];
    appendAny("total count " + s.filter(function(string s) returns (boolean){return lengthof s == 3;}).count());
    int i = s.count() + r.count();
    return (output, i);
}

function testInFunctionInvocation () returns (int) {
    map m = { a: "abc", b: "cd", c :"pqr"};
    return doubleInt(m.filter(function (any a) returns (boolean) {
                                    var s =? <string> a;
                                    return lengthof s == 3;})
                     .count());
}

function doubleInt (int i) returns (int) {
    return i * 2;
}

function testInStatement() returns (int){
    map m = { a: "abc", b: "cd", c :"pqr"};
    if(5 > m.filter(function (any a) returns (boolean) {
                                  var s =? <string> a;
                                  return lengthof s == 3;})
                     .count()){
        return 10;
    }
    return 0;
}
