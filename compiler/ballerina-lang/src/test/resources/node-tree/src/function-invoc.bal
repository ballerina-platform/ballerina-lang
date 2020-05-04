import ballerina/io;

public function main(string... args) {
    testInvocation();
}

function testInvocation(){
   // qulified invocation
   print(111);
   print(222, true);
   print(333, true, "string");
}

// function definition with defaultable and rest parameters
function print(int a, boolean b = true, string...c) {
   // fully qulified invocation
   io:println(a);
   io:println(b);
   io:println(c);
}
