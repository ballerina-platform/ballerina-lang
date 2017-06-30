import ballerina.lang.system;

function main (string[] args) {

    int i;
    string s;
    long currentTime;
    long epochTime;
    long nanoTime;

    i = 10;
    s = "hello";

    //print a value.
    //value can be string, int, long, float, boolean, double.
    system:println(s);
    system:print(s);
    // output is equal to s\ns

    //log a value.
    //value can be string, int, long, float, boolean, double.
    //first parameter is log level.
    //second parameter is log message.
    //Log level info.
    // 1 - TRACE
    // 2 - DEBUG
    // 3 - INFO
    // 4 - WARN
    // 5 - ERROR
    system:log(3, i);

    currentTime = system:currentTimeMillis();
    system:log(3, currentTime);
    epochTime = system:epochTime();
    system:log(3, epochTime);
    nanoTime = system:nanoTime();
    system:log(3, nanoTime);

}