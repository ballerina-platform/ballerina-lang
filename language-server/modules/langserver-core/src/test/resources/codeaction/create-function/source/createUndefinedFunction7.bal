import ballerina/module1;
import ballerina/math;

type Fruit object {
    function name() returns int {
        return 4;
    }
};

function foo(string filePath) returns error? {
    (function (string) returns boolean) spliter = p => math:random() > .5;
    string recs = "";
    string[][][] p = partition(recs, "district", spliter);

    int a = 12;
    int b = 13;
    [int, string[][]] tup = addAndPrintTwoIntegers(a, b);
    int result = addAndGetResultOfTwoIntegers(a, b);

    module1:Listener c = new (8080);
    module1:TestClass1 res = callEndpoint(a, b, c);

    float nanVal = 0.0 / 0.0;
    printThis(nanVal.isNaN());
    Fruit fruit = new Fruit();
    printField(fruit.name());

    record {
      string recField = "";
    } testRec = {};
    record {
      int recField = 1;
    } testRec2 = thisIsANewFunction(testRec);
}
