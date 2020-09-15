
function testAnonFunc() returns string {
    function (string, string) returns (string) anonFunction =
                function (string x, string y) returns (string) {
                    return x + y;
                };
    return anonFunction("Hello ", "World.!!!");
}

function testFPPassing() returns int {
    function (int, string)  anonFunction =
                    function (int x, string y) {
                        string z = y + y;
                    };
    var fp = useFp(anonFunction);
    return fp(10, 20);
}

function useFp(function (int, string) fp) returns (function (int, int) returns (int)) {
    fp(10, "y");
    function (int, int) returns (int) fp2 =
                        function (int x, int y) returns (int) {
                            return x * y;
                        };
    return fp2;
}

function testBasicClosure() returns int {
    var foo = basicClosure();
    return foo(3);

}

function basicClosure() returns (function (int) returns int) {
    int a = 3;
    var foo = function (int b) returns int {
        int c = 34;
        if (b == 3) {
            c = c + b + a + 5;
        }
        return c + a;
    };

    a = 100;
    return foo;
}

function testMultilevelClosure() returns int {
     var bar = multilevelClosure();
     return bar(5);
}

function multilevelClosure() returns (function (int) returns int) {
    int a = 2;
    var func1 = function (int x) returns int {
        int b = 23;

        a = a + 8;
        var func2 = function (int y) returns int {
            int c = 7;
            var func3 = function (int z) returns int {

                b = b + 1;
                return x + y + z + a + b + c;
            };
            return func3(8) + y + x;
        };
        return func2(4) + x;
    };
    return func1;
}

function basicWorkerTest() returns int {
    int global = 10;

    worker w1 {
       global += 10;
    }

    worker w2 returns int {
       int i = 0;
       while(i < 100) {
          i +=1;
       }
       return i;
    }

    int result = wait w2;
    wait w1;
    
    return global + result;
}

public function testQualifiers() {
    foo(isolated function () {

    });
}

function foo(isolated function() x) {

}
