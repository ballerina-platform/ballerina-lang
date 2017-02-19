import ballerina.lang.system;

function testPrintAndPrintlnString(string s1, string s2){
    system:println(s1);
    system:print(s2);
    // output is equal to s1\ns2
}

function testPrintAndPrintlnLong(long v1, long v2){
    system:println(v1);
    system:print(v2);
    // output is equal to v1\nv2
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

function testPrintAndPrintlnBoolean(double v1, double v2){
    system:println(v1);
    system:print(v2);
    // output is equal to v1\nv2
}

function testPrintAndPrintlnDouble(boolean v1, boolean v2){
    system:println(v1);
    system:print(v2);
    // output is equal to v1\nv2
}

function testLog(long l, double d) {
    int i;
    //long l; // Related to issue #679
    float f;
    //double d; // Related to issue #679
    string s;
    boolean b;

    i = 10;
    //l = 100; // Related to issue #679
    f = 10.1f;
    //d = 10.1; // Related to issue #679
    s = "hello";
    b = false;

    system:log(1, i);
    system:log(2, l);
    system:log(3, f);
    system:log(4, d);
    system:log(5, s);
    // should not log.
    system:log(6, b);
}

function testTimeFunctions()(long, long, long) {

    long currentTime;
    long epochTime;
    long nanoTime;

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

function getEnvVar(string varName) {
    string pathValue = system:getEnv(varName);
    system:print(pathValue);
}
