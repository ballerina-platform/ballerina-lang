import a.b;
import foo;
import bar;

function test1() returns (int){
    function (int, int) returns (int) addFunction = b:Fn1();
    return addFunction(1, 2);
}

function test2() returns (int){
    function (int, int) returns (int) addFunction = b:Fn2();
    return addFunction(1, 2);
}


public function getCombinedString() returns string {
    var unifier = function (function () returns string get) returns string {
        var j = get();
        return j;
    };
    // works as expected if only one of the unifier() functions is called
    string res = "";
    res += unifier(foo:data);
    res += " ";
    res += unifier(bar:data);
    return res;
}
