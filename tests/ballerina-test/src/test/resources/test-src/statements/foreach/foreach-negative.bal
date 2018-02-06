function test1(){
    string[] data = ["mon", "tue", "wed", "thu", "fri"];
    foreach i, s, f in data {
        println(i + " " + s + " " + f);
    }
}

function test2(){
    string[] data = ["mon", "tue", "wed", "thu", "fri"];
    float i = 10;
    boolean s = true;
    foreach i, s in data {
        println(i + " " + s);
    }
}

function test3(){
    string[] data = ["mon", "tue", "wed", "thu", "fri"];
    foreach i, _ in data {
        println(i + " ");
    }
    println(i);
}

function test4(){
    string vals = "values";
    foreach s in vals {
        string s1 = s + s;
        println(s1);
    }
}

struct person {
    int id;
}

function test5(){
    string[] data = ["mon", "tue", "wed", "thu", "fri"];
    person p = {};
    foreach p.id, s in data {
        string s1 = s + s;
        println(s1);
    }
}

function test6(){
    string[] data = ["mon", "tue", "wed", "thu", "fri"];
    foreach _, _, _ in data {
        println("hello");
    }
}

function test7(){
    foreach i in "a".."z" {
        println(i);
    }
}

function test8(){
    json j = ["a" , "b", "c"];
    var a,_ = <json[]> j;
    foreach x,y in a {
        print(x);
        println(y);
    }
}

function test9(){
    string[] slist = ["a" , "b", "c"];
    foreach v in slist {
        println(v);
        break;
        println(v);
    }
    foreach y in slist {
        println(y);
        next;
        println(y);
    }
    next;
    println("done");
}

function main () {
   println("done");
}
