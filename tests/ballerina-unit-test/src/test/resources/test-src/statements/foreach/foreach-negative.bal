import ballerina/io;
function test1(){
    string[] data = ["mon", "tue", "wed", "thu", "fri"];
    foreach i, s, f in data {
        io:println(i + " " + s + " " + f);
    }
}

function test2(){
    string[] data = ["mon", "tue", "wed", "thu", "fri"];
    float i = 10.0;
    boolean s = true;
    foreach i, s in data {
        io:println(i + " " + s);
    }
}

function test3(){
    string[] data = ["mon", "tue", "wed", "thu", "fri"];
    foreach i, _ in data {
        io:println(i + " ");
    }
    io:println(i);
}

function test4(){
    string vals = "values";
    foreach s in vals {
        string s1 = s + s;
        io:println(s1);
    }
}

type person record {
    int id = 0;
};

function test5(){
    string[] data = ["mon", "tue", "wed", "thu", "fri"];
    person p = {};
    foreach p.id, s in data {
        string s1 = s + s;
        io:println(s1);
    }
}

function test6(){
    string[] data = ["mon", "tue", "wed", "thu", "fri"];
    foreach _, _, _ in data {
        io:println("hello");
    }
}

function test8() returns error? {
    json j = ["a" , "b", "c"];
    var jsonArray = <json[]> j;
    if (jsonArray is json[]) {
        foreach x,y in jsonArray {
            io:print(x);
            io:println(y);
        }
    } else if (jsonArray is error) {
        return jsonArray;
    }
    return ();
}

function test9(){
    string[] slist = ["a" , "b", "c"];
    foreach v in slist {
        io:println(v);
        break;
        io:println(v);
    }
    foreach y in slist {
        io:println(y);
        continue;
        io:println(y);
    }
    continue;
    io:println("done");
}

public function main () {
    io:println("done");
}
