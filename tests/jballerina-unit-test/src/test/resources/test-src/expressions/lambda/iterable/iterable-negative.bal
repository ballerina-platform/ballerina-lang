int count = 0;
string word = "";

function test1(){
    int x = 0;
    x.foreach(function (int i) { count = count + i;}); // Iterating on invalid type
    string y = "foo";
    y.map(function (string s) returns (int) { return s.length();}); // Iterating on invalid type
}

function test2(){
    string[] y = ["1", "a"];

    y.count(); // Not assigning the return value.

    y.filter(function (int i, string x) returns boolean { // Too many arguments to lambda.
        return true;})
     .foreach(function (string x) { word = x;}).count();
}

function test3(){
    map<string> z = {a:"1", b:"2"};
    string[] keys = z.map(
                     function (string s) returns (string, string) { // Not enough arguments to lambda.
                         return (s, "value");
    }).keys();
}

function test4() {
    map<any> z = {a:"1", b:"2"};
    map<string> a = z.map(function (any x) returns (string, string) {
                           var s = <string>x;
                           return (s, "value");
                       });
    map<any> m = z.filter(function (string s) returns boolean {
          return s == "";
    });
    any x = z.filter(function (string s) returns boolean {
         return s == "";
     });
}

function test5(){
    string[] s = ["1", "a"];
    int x;
    x = s.foreach(function (string s) {word = word + s;});

    var (z, y) = s.map(function ((int, string) tuple) returns (int, string) { var (i, v) = tuple;
                           return (i * 2, v + v);
                       });
}

function test6(){
    string[] s = ["1", "a"];
    _ = s.count(test5);
    s.filter();
    int i = 10;
    s.foreach(i);
}

function test7(){
    string[] s = ["foo", "bar"];
    s.foreach(function ((string, string, string) z) {});
    s.foreach(function () {});
    s.filter(function (string s) returns (boolean, int) {return (true, 1);});
    s.filter(function (string s) {});
    s.filter(function (person p) {});
    _ = s.filter(function (string s) returns (person) {return ();});
}

function test8() {
    int[] arr = [-5, 2, 4, 5, 7, -8, -3, 2];
    int[] a = arr.map(function (int v) returns (any) {
                        return v + 1;
                   });
}

function test9() {
    int[] arr = [-5, 2, 4, 5, 7, -8, -3, 2];
    int[] a = arr.map(function (int v) returns (int) {
                        return v + 1;
                   }).map(function (int v) returns (string) {
                        return "Test" + v;
                   });
}

function test10() {
    int[] arr = [-5, 2, 4, 5, 7, -8, -3, 2];
    int[] a = arr.map(function (int v) returns (int) {
                        return v + 1;
                   }).map(function (int v) returns (string) {
                        return "Test" + v;
                   }).filter(function (string s) returns boolean { return true;});
}

function test11() {
    map<any> z = {a:"1", b:"2"};

    map<any> m = z.filter(function (any s) returns boolean {
          return s == ();
    });

    any x = z.filter(function (any s) returns boolean {
         return s == ();
    });
}

function testVarInLHS() {
    int[] numbers = [-5, -3, 2, 7, 12];
    var filtered = numbers.filter(function (int i) returns boolean {
            return i >= 0;
        });
    float avg = filtered.average();
}

function testVarInLHS2() {
    int[] numbers = [-5, -3, 2, 7, 12];
    var mapped = numbers.map(function(int value) returns float {
            return float.convert(value);
        }).filter(function (float value) returns boolean {
            return value > 0;
        }).map(function (float value) returns (string, float) {
            return (string.convert(value), value);
        });
}

function testIndexBasedAccess() {
    int[] numbers = [-5, -3, 2, 7, 12];
    var x = numbers.filter(function (int i) returns boolean {
            return i >= 0;
        })[0];
}

int[] globalNumbers = [-5, -3, 2, 7, 12];

function testAnydataInLHS() {
    anydata mapped = globalNumbers.map(function(int value) returns float {
               return float.convert(value);
           }).filter(function (float value) returns boolean {
               return value > 0;
           }).map(function (float value) returns (string, float) {
               return (string.convert(value), value);
           });
}
