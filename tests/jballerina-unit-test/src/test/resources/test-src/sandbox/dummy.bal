import ballerina/jballerina.java;

function dummyTest() returns error? {
    int[][] data = [[1,2], [2,3,4]];
    int sumofEven = 0;
    check  from int[] arr in data do { // Query Action
        //int[] arrs = arr;
        //print(arrs);
        int[] evenNumbers = from int i in arr
        //where i % 2 == 0
        select i; // Query expression
        //check from int i in evenNumbers do {
        //    sumofEven += i;
        //};
    };
}




















public function print(anydata value) = @java:Method {
    'class: "org.ballerinalang.test.sandbox.DummyTest",
    name: "print"
} external;
