function test1()(int){
    function ( int , int ) returns ( int ) addFunction = func1;
    return addFunction(1, 2);
}

function func1 (int a, int b) (int c) {
    c = a + b;
    return;
}

function test2 () (string) {
    function ( int , int )( string ) sumFunction = function(int a, int b)(string){int value =  a + b;
                                       return "sum is " + value;
                                   };
    return sumFunction(1,2);
}

function test3()(int){
    int x = test3Callee(1, func1);
    return x;
}

function test3Callee(int a, function ( int x, int y) returns ( int z) func )   returns ( int){
    int x = a + func(1, 2);
    return x;
}

function test4()(string){
    function ( string a, string b)( string ) foo = test4Callee();
    string v = foo("hello ", "world.");
    return v;
}

function test4Callee()(function ( string a, string b)( string )){
   return function(string x, string y)(string){string z = x + y;
             return z;
          };
}

function test5()(string){
    function ( string , float )( string ) bar = test5Callee();
    return "test5 " + bar("string", 1.0);
}

function test5Callee()(function ( string , float )( string )){
       return test5Ref;
}

function test5Ref(string a, float b)(string c){
    c = a + b;
    return;
}

function test6()(string){
    function ( string , function ( string , float )( string ) )( string ) foo = test6Callee();
    return foo("test6 ", test5Ref);
}

function test6Callee()(function ( string , function ( string , float )( string ) )( string )){
       return test6Ref;
}

function test6Ref(string a, function ( string , float )( string ) b)(string c){
    c = a + b(a , 1.0);
    return;
}
