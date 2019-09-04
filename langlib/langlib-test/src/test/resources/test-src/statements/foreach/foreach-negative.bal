import ballerina/lang.'error as lang;

string output = "";

json jdata = {
    name: "bob",
    age: 10,
    pass: true,
    subjects: [
        { subject: "maths", marks: 75 },
        { subject: "English", marks: 85 }
    ]
};

type Detail record {
    *lang:Detail;
    boolean fatal;
};

type Employee record {
    int id;
    string name;
    float salary;
};

table<Employee> data = table {
    { key id, name, salary },
    [
        { 1, "Mary",  300.5 },
        { 2, "John",  200.5 },
        { 3, "Jim", 330.5 }
    ]
};

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

function test11() {
    [[string, string], [string, string], [string, string]] sTuple = [["ddd", "d1"], ["rrr", "d1"], ["fef", "d1"]];
    output = "";
    int i = 0;
    foreach var [v, u] in sTuple {
        v = "GG";
        if (i == 1) {
            output = output + "continue ";
            i += 1;
            continue;
        }
        concatString(i, v);
        i += 1;
    }
}

function test12() {
    error<string, Detail> err1 = error("Error One", message = "msgOne", fatal = true);
    error<string, Detail> err2 = error("Error Two", message = "msgTwo", fatal = false);
    error<string, Detail> err3 = error("Error Three", message = "msgThree", fatal = true);
    error<string, Detail>[3] errorArray = [err1, err2, err3];

    string result1 = "";
    foreach var error(reason, message = message, fatal = fatal) in errorArray {
        reason = "updated reason";
        fatal = false;
        message = "msgNew";
    }
}

function test13() {
    output = "";
    int i = 0;
    foreach var {id, name, salary} in data {
        id = 2;
        name = "John";
        salary = 250.5;
    }
}

function test14() returns string {
    output = "";
    json subjects = <json>jdata.subjects;

    int i = 0;
    if subjects is json[] {
        foreach var v in subjects {
            v = {};
        }
    }
    return output;
}

function test15() returns string {
    output = "";

    Employee d = { id: 1, name: "AbuTharek", salary: 100.0 };

    int i = 0;
    foreach any v in d {
        if (v is string) {
            v = "Kanaka";
        }
    }
    return output;
}

function test17() {
    output = "";
    int i = 0;
    Employee d1 = { id: 1, name: "Abu", salary: 1000.0, "married": false };
    Employee d2 = { id: 2, name: "Tharek", salary: 1000.0, "married": false };
    Employee d3 = { id: 3, name: "Kanaka", salary: 1000.0, "married": false };
    Employee[] data = [d1, d2, d3];

    foreach var {id, name, salary, ...status} in data {
        status = {};
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

function concatString(int index, string value) {
    output = output + index.toString() + ":" + value + " ";
}
