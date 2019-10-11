string output = "";

function test(){
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

function println(any... v) {
     output = v[0].toString();
}

function print(any... v) {
    output = v[0].toString();
}
