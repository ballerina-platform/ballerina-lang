import ballerina/jballerina.java;

function dummyTest() returns error? {
    int[][] data = [[1,2], [2,3,4]];
    int sumofEven = 0;

    error? res1 =  from int[] arr in data do { // Query Action
        error? res2 =  from int i in arr
       do {
           print(i);
       };
    };

    // var res = from int[] arr in data
    // from int i in arr
    // select i;

}




















public function print(anydata value) = @java:Method {
    'class: "org.ballerinalang.test.sandbox.DummyTest",
    name: "print"
} external;
