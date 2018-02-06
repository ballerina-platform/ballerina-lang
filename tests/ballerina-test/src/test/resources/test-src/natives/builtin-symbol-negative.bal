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

string StackFrame = "abc";

function test1(){
    int Time = 10;
}

function <error e> getMessage() returns (string){
    return e.msg;
}

function test2(){
    function (any a) foo = print;
    foo("abc");
}

struct NullReferenceException {
    string msg;
}

enum Time {
    VALUE1,
    VALUE2
}
