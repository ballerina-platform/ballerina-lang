function process(function (int, int) returns int func, int num1, int num2) returns int{
    return func(num1,num2);
}

public function subtractFunction(int a, int b) returns int {
    return a-b;
}

public function getFunction() returns function(int,int) returns int {
    return subtractFunction;
}

public function main() {
    var addFunction = function (int num1, int num2) returns int {
                return num1 + num2;
            };
    int myInt1 = 1;
    int myInt2 = 2;
    int total = process();
}
