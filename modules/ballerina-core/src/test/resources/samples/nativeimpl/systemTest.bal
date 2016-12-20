function invokeNativeFunction(string s1, string s2){
    ballerina.lang.system:println(s1);
    ballerina.lang.system:print(s2);
    // output is equal to s1\ns2
}