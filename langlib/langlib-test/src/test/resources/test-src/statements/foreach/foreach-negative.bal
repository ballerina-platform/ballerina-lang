string output = "";
function test1(){
    string[] data = ["mon", "tue", "wed", "thu", "fri"];
    foreach var [i, s, f] in data {
        println(i + " " + s + " " + f);
    }
}

function test2(){
    string[] data = ["mon", "tue", "wed", "thu", "fri"];
    float i = 10.0;
    boolean s = true;
    foreach var [i, s] in data {
        println(i + " " + s);
    }
}

function test3(){
    string[] data = ["mon", "tue", "wed", "thu", "fri"];
    foreach var [i, j] in data {
        println(i + " ");
    }
    println(i);
}

function test4(){
    int vals = 1000;
    foreach var s in vals {
        string s1 = s + s;
        println(s1);
    }
}

type person record {
    int id = 0;
};

function test5(){
    string[] data = ["mon", "tue", "wed", "thu", "fri"];
    person p = {};
    foreach var [i, s] in data {
        string s1 = s + s;
        println(s1);
    }
}

function test6(){
    string[] data = ["mon", "tue", "wed", "thu", "fri"];
    foreach var [i, j, k] in data {
        println("hello");
    }
}

function test8() returns error? {
    json j = ["a" , "b", "c"];
    var jsonArray = <json[]> j;

    foreach var [x, y] in jsonArray {
        print(x);
        println(y);
    }

    return ();
}

function test9(){
    string[] slist = ["a" , "b", "c"];
    foreach var v in slist {
        println(v);
        break;
        println(v);
    }
    foreach var y in slist {
        println(y);
        continue;
        println(y);
    }
    continue;
    println("done");
}

function test10(){
    string[] data = ["mon", "tue", "wed", "thu", "fri"];
    foreach var [i, {j, k: {l, m}}] in data {
        println(i + " " + j + " " + l + " " + m);
    }
}

public function main () {
    println("done");
}

function println(any... v) {
     output = v[0].toString();
}

function print(any... v) {
    output = v[0].toString();
}