import ballerina/test;
import ballerina/config;
// This tests the functionality of reading config

@test:Config{
}
function test1 () {
     string userName = config:getAsString("user.name");
     test:assertEquals(userName, "waruna");
}

