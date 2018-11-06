int count;
string word;

function test1(){
    int x = 0;
    x.foreach(function (int i) { count = count + i;});
    string y = "foo";
    y.map(function (string s) returns (int) { return s.length();});
}

function test2(){
    string[] y = ["1", "a"];

    y.count();

    y.filter(function (int i, string x) returns (boolean) {
        return true;})
     .foreach(function (string x) { word = x;}).count();
}

function test3(){
    map<string> z = {a:"1", b:"2"};
    string[] keys = z.map(
                     function (string s) returns (string, string) {
                         return (s, "value");
    }).keys();
}

function test4() {
    map z = {a:"1", b:"2"};
    string[] a = z.map(function (any x) returns (string, string) {
                           var s = <string>x;
                           return (s, "value");
                       });
    map m = z.filter(function (string s) returns boolean {
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
    _ = s.filter(function (string s) returns (person) {return null;});
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
    map z = {a:"1", b:"2"};

    map m = z.filter(function (any s) returns boolean {
          return s == null;
    });

    any x = z.filter(function (any s) returns boolean {
         return s == null;
    });
}
