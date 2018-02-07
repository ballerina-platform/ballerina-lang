string out;

function test1(){
    string[] s = ["foo", "bar"];
    s.filter();
    int i = 10;
    s.foreach(i);
}

function test2(){
    string[] s = ["foo", "bar"];
    s.foreach(function (string x, string y, string z){});
    s.foreach(function (){});
}
