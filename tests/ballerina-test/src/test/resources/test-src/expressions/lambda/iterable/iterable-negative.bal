int count;
string word;

function test1(){
    int x = 0;
    x.foreach(function (int i) { count = count + i;});
    string y = "foo";
    y.map(function (string s) (int) { return lengthof s;});
}

function test2(){
    string[] y = ["1", "a"];

    y.count();

    y.filter(function (int i, string x)(boolean){
        return true;})
     .foreach(function (string x){ word = x;}).count();
}

function test3(){
    map z = { a : "1", b : "2"};
    string[] keys = z.map(function (any x)(string, string){
          var s, _ = (string) x;
          return s, "value";
    }).keys();
}

function test4() {
    map z = { a : "1", b : "2"};
    string[] a = z.map(function (any x)(string, string){
          var s, _ = (string) x;
          return s, "value";
    });
    map m = z.filter(function (any x)(boolean ){
          var s, _ = (string) x;
          return s == null;
    });
    any x = z.filter(function (any x)(boolean ){
         var s, _ = (string) x;
         return s == null;
     });
}

function test5(){
    string[] s = ["1", "a"];
    int x;
    x = s.foreach(function (string s){word = word + s;});
    string y;
    x, y = s.map(function (int i, string v)(int, string){ return i*2, v + v;});
}

function test6(){
    string[] s = ["1", "a"];
    s.count(test5);
}
