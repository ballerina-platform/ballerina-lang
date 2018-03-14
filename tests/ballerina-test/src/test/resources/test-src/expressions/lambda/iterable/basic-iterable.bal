int add;

function testInt1()(int fadd, int count, int max, int min, int sum, float avg){
    add = 0;
    int[] fa = [-5, 2, 4, 5, 7, -8, -3 , 2];
    fa.foreach(function (int i){ add = add + i;});
    fadd = add;
    count = fa.count();
    max = fa.max();
    min = fa.min();
    sum = fa.sum();
    avg = fa.average();
    return;
}

function testInt2()(int count, int max, int min, int sum, float avg){
    int[] fa = [-5, 2, 4, 5, 7, -8, -3 , 2];
    count = fa.filter(filterIntNegative).count();
    max = fa.filter(filterIntNegative).max();
    min = fa.filter(filterIntNegative).min();
    sum = fa.filter(filterIntNegative).sum();
    avg = fa.filter(filterIntNegative).average();
    return;
}

function filterIntNegative(int i)(boolean){
    return i >= 0;
}

function testFloat1()(int fcount, int count, float max, float min, float sum, float avg){
    add = 0;
    float[] fa = [1.1, 2.2, -3.3, 4.4, 5.5];
    fa.foreach(function (int i){ add = add + i;});
    fcount = add;
    count = fa.count();
    max = fa.max();
    min = fa.min();
    sum = fa.sum();
    avg = fa.average();
    return;
}

function testFloat2()(int count, float max, float min, float sum, float avg){
    float[] fa = [1.1, 2.2, -3.3, 4.4, 5.5];
    count = fa.filter(filterFloatNegative).count();
    max = fa.filter(filterFloatNegative).max();
    min = fa.filter(filterFloatNegative).min();
    sum = fa.filter(filterFloatNegative).sum();
    avg = fa.filter(filterFloatNegative).average();
    return;
}

function filterFloatNegative(float i)(boolean){
    return i >= 0;
}

string output;

function testBasicArray1 (string[] values) (string) {
    output = "";
    values.map(mapString).foreach (concat);
    return output.trim();
}

function mapString (string x) (string up, string lower) {
    up = x.toUpperCase();
    lower = x.toLowerCase();
    return;
}

function concat (string up, string lower) {
    output = output + " " + up + ":" + lower;
}

function testBasicArray2 (string[] values) (string) {
    output = "";
    values.map(function (int a, string b) (string s) {
                    s = a + b;
                    return;
                })
               .foreach (function (string s){
                    output = output + s + " ";
                });
    return output.trim();
}

function testBasicMap1 () (int count, string[] values) {
    map m = {a:"A", b:"B", c:"C", d:"D", e:"E"};
    count = m.count();
    values = m.map(function (any value)(string) {
                    var v, _ = (string)value;
                    return v;
                })
                .filter(function (string v) (boolean) {
                       if (v == "A" || v == "E") {
                           return true;
                       }
                       return false; });
    return;
}

function testBasicMap2 () (string[] values) {
    map m = {a:"A", b:"B", c:"C", d:"D", e:"E"};
    values = m.map(mapAnyToString)
                .filter(function (string k, string v) (boolean) {
                       if (k == "a" || k == "e") {
                           return true;
                       }
                       return false; })
                .map(concatString);
    return;
}

function mapAnyToString (string key, any value) (string, string) {
    var v, _ = (string)value;
    return key, v;
}

function concatString (string v1, string v2) (string) {
    return v1 + v2;
}

json j1 = {name:"bob", age:10, pass:true, subjects:[{subject:"maths", marks:75}, {subject:"English", marks:85}]};
function jsonTest()(string, string[], int, any, string[]){
    output = "";
    j1.foreach(function (json j){
        output = output + j.toString();
    });

    string[] sa = j1.map(function (json j)(string){
        return j.toString();
    })
    .filter(function (string s)(boolean){
        return s == "bob";
    });

    int i = j1.count();

    var ja, _ = (json[]) j1.subjects;
    string[] result = ja.map(function (int i, json j)(string){
        return i + "->" + j.toString();
    });

    return output, sa, i, j1.count(), result;
}

xml xdata = xml `<p:person xmlns:p="foo" xmlns:q="bar">
        <p:name>bob</p:name>
        <p:address>
            <p:city>NY</p:city>
            <q:country>US</q:country>
        </p:address>
        <q:ID>1131313</q:ID>
    </p:person>`;

function xmlTest()(int nodeCount, any elementCount, map m){
    nodeCount = xdata.children().count();
    elementCount = xdata.children().elements().count();
    m = xdata.children().elements()[1].children().elements().map(function (int i, xml x)(string, xml){
        return <string>i, x;
    });
    return;
}

struct person {
    string name;
    int age;
}

function structTest()(int count, string[] names){
    person bob = { name: "bob", age: 30};
    person tom = { name: "tom", age: 20};
    person sam = { name: "sam", age: 24};
    person[] p = [bob, tom, sam];
    count = p.filter(isBellow25).count();
    names = p.map(getName);
    return;
}

function getName(person p)(string s){
    return p.name;
}

function isBellow25(person p)(boolean){
    return p.age < 25;
}

function testIgnoredValue()(string x){
    output = "";
    string[] s = ["abc", "cd", "pqr"];
    _ = s.filter(function(string s)(boolean){return lengthof s == 3;})
            .map(function(string s)(string){
                     output = output + " " + s;
                     return s + s;
                     });
    return output.trim();
}

function appendAny(any a){
    var s, _ = (string) a;
    output = s;
}

function testInExpression()(string, int){
    output = "";
    string[] s = ["abc", "cd", "pqr"];
    float[] r = [1.1, -2.2, 3.3, 4.4];
    appendAny("total count " + s.filter(function(string s)(boolean){return lengthof s == 3;}).count());
    int i = s.count() + r.count();
    return output, i;
}

function testInFunctionInvocation () (int) {
    map m = { a: "abc", b: "cd", c :"pqr"};
    return doubleInt(m.filter(function (any a) (boolean) {
                                    var s,_ = (string) a;
                                    return lengthof s == 3;})
                     .count());
}

function doubleInt (int i) (int) {
    return i * 2;
}

function testInStatement() (int){
    map m = { a: "abc", b: "cd", c :"pqr"};
    if(5 > m.filter(function (any a) (boolean) {
                                  var s,_ = (string) a;
                                  return lengthof s == 3;})
                     .count()){
        return 10;
    }
    return 0;
}