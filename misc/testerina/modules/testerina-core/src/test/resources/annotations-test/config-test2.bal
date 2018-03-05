import ballerina.test;
import ballerina.io;

function myfunction (int k) (int l){
    l = k + 1;
    io:println(l);
    return l;
}

@test:mock {
    functionName : "myfunction"
}
function mockReset (int k) (int){
    io:println("Im the mock");
    return 5;
}

@test:config{
}
function testMock () {
    int x = myfunction(2);
    io:println(x);
}



