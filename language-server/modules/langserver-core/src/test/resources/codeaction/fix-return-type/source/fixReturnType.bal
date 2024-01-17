function addTwoIntegers(int a, int b) {
   return 100;
}

function addTwoIntegers2(int a, int b) returns () {
   return [100, ""];
}

function addTwoIntegers3(int a, int b) returns boolean {
   int[] d = [];
   return d;
}

public function testFunctionWithObjectConstructor() {
    var var1 = object {
        int id = 10;
        function getId() {
            return self.id;
        }
    };
}

function returnIntWithError() returns int|error {
    return 10;
}

public function testCheckExprReturnType(int x) returns error {
    if (x == 2) {
        _ = check returnIntWithError();
    }
    return "string";
}
