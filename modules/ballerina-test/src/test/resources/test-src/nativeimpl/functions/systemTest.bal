import ballerina.lang.system;

function testPrintAndPrintlnString(string s1, string s2){
    system:println(s1);
    system:print(s2);
    // output is equal to s1\ns2
}

function testPrintAndPrintlnInt(int v1, int v2){
    system:println(v1);
    system:print(v2);
    // output is equal to v1\nv2
}

function testPrintAndPrintlnFloat(float v1, float v2){
    system:println(v1);
    system:print(v2);
    // output is equal to v1\nv2
}

function testPrintAndPrintlnBoolean(boolean v1, boolean v2){
    system:println(v1);
    system:print(v2);
    // output is equal to v1\nv2
}

function testTimeFunctions()(int, int, int) {

    int currentTime;
    int epochTime;
    int nanoTime;

    currentTime = system:currentTimeMillis();
    epochTime = system:epochTime();
    nanoTime = system:nanoTime();
    return currentTime, epochTime, nanoTime;
}

function testDateFunction()(string) {

    string shortDate;

    shortDate = system:getDateFormat("yyyyMMdd");
    return shortDate;

}

function printNewline() {
    system:print("hello\n");
}

function testSleep(int timeoutv) {
    system:sleep(timeoutv);
}

function getEnvVar(string varName) {
    string pathValue = system:getEnv(varName);
    system:print(pathValue);
}
