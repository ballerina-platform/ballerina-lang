public function main() {
    var addFunction = function (int num1, int num2) returns int {
                return num1 + num2;
            };
    int myInt1 = 1;
    int myInt2 = 2;
    string myString = "hello";

    MyType var1 = {
            a:10,
            b:5
        };

    int total = myFunc();
}

function myFunc(MyType a) returns int {
    return 10;
}

public function process4() returns MyType {
    MyType a = {
        a:10,
        b:5
    };
    return a;
}

type MyType record {|
    int a;
    int b;
|};
