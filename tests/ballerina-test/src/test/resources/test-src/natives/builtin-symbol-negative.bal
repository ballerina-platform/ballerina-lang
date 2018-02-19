function test0 (){
    break;
}

function error (string x, error e){
    print(x);
}

function println(any a){
    print(a);
    print("\n");
}



function test1(){
    int Time = 10;
}

function <error e> getMessage() returns (string){
    return e.message;
}

function test2(){
    function (any a) foo = print;
    foo("abc");
}

enum Time {
    VALUE1,
    VALUE2
}
