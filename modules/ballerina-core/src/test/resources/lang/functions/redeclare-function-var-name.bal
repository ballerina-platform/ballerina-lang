function redeclareString () (string){
    string foo = "Hello ";

    return foo+foo();
}

function foo()(string param) {
    return "World";
}

function redeclareInt()(int){
    int bar = 5;

    return bar+bar();
}

function bar()(int bar) {
    return 3;
}

function multiple()(boolean){
    int total = 9;
    int result;
    if(total(total)==total){
        return true;
    }else{
        return false;
    }
}

function total(int x)(int a) {
    return x;
}

