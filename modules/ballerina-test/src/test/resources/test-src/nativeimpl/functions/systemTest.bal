import ballerina.lang.system;

function testPrintAndPrintlnString(string s1, string s2){
    println(s1);
    print(s2);
    // output is equal to s1\ns2
}

function testPrintAndPrintlnInt(int v1, int v2){
    println(v1);
    print(v2);
    // output is equal to v1\nv2
}

function testPrintAndPrintlnFloat(float v1, float v2){
    println(v1);
    print(v2);
    // output is equal to v1\nv2
}

function testPrintAndPrintlnBoolean(boolean v1, boolean v2){
    println(v1);
    print(v2);
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
    print("hello\n");
}

function testSleep(int timeoutv) {
    sleep(timeoutv);
}

function getEnvVar(string varName) {
    string pathValue = getEnv(varName);
    print(pathValue);
}
