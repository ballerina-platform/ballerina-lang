int count;
string word;

function test1(){
    int x = 0;
    x.foreach((int i) => { count = count + i;});
    string y = "foo";
    y.map((string s) => (int) { return lengthof s;});
}

function test2(){
    string[] y = ["1", "a"];

    y.count();

    y.filter((int i, string x) => (boolean) {
        return true;})
     .foreach((string x) => { word = x;}).count();
}

function test3(){
    map<string> z = {a:"1", b:"2"};
    string[] keys = z.map(
                     (string s) => (string, string) {
                         return (s, "value");
    }).keys();
}

function test4() {
    map z = {a:"1", b:"2"};
    string[] a = z.map((any x) => (string, string) {
                           var s = <string>x;
                           return (s, "value");
                       });
    map m = z.filter((string s) => boolean {
          return s == null;
    });
    any x = z.filter((string s) => boolean {
         return s == null;
     });
}

function test5(){
    string[] s = ["1", "a"];
    int x;
    x = s.foreach((string s) => {word = word + s;});

    var (x, y) = s.map(((int, string) tuple) => (int, string) { var (i, v) = tuple;
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
    s.foreach(((string, string, string) z) => {});
    s.foreach(() => {});
    s.filter((string s) => (boolean, int) {return (true, 1);});
    s.filter((string s)=>{});
    s.filter((person p)=>{});
    _ = s.filter((string s) => (person) {return null;});
}
