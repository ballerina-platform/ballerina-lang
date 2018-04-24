import ballerina/io;
import ballerina/time;

function main(string... args) {

    if (lengthof args < 3) {
        io:println("ERROR: Please specify the number of warm-up iterations and benchmark iterations.");
        return;
    }

    var warmupIterations = check <int>args[0];
    var benchmarkIterations = check <int>args[1];

    string functionName = args[2];
    addFunctions();
    function() func = getFunction(functionName);
    executeBenchmark(func, functionName, warmupIterations, benchmarkIterations);
}

function executeBenchmark(function () f, string functionName, int warmupIterations, int benchmarkIterations) {

    int i = 0;
    while (i < warmupIterations) {
        i = i + 1;
        f();
    }

    i = 0;
    int startTime = time:nanoTime();
    while (i < benchmarkIterations) {
        i = i + 1;
        f();
    }
    int endTime = time:nanoTime();
    io:print(functionName + ",");

    float totalTime = (endTime - startTime);
    float totalTimeMilli = (totalTime / 1000000.0);
    io:print(io:sprintf("%10.2f,", totalTimeMilli), 0);

    float avgLatency = (<float>totalTime / <float>benchmarkIterations);
    io:print(io:sprintf("%10.2f,", avgLatency), 0);

    float tps = (1000000000.0 / avgLatency);
    io:println(io:sprintf("%10.2f", tps), 0);
}

