import ballerina/io;
import ballerina/time;
import ballerina/file;

function main(string... args) {

    var warmupIterations = check <int>args[0];
    var benchmarkIterations = check <int>args[1];
    string resultFileName = untaintedReturn(<string>args[2]);

    string functionName = args[3];
    addFunctions();

    function() func = getFunction(functionName);

    executeBenchmark(func, functionName, warmupIterations, benchmarkIterations,
        resultFileName);
}

function executeBenchmark(function () f, string functionName, int warmupIterations,
int benchmarkIterations, string resultsFileName) {

    io:println("Executing function " + functionName + " " + (warmupIterations + benchmarkIterations) + " times.Please wait...");

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

    string resultsFileLocation = "results/" + resultsFileName + ".csv";
    io:ByteChannel channel = io:openFile(resultsFileLocation, "a");
    io:CharacterChannel charChannel = new io:CharacterChannel(channel, "UTF-8");
    int resultWrite = check charChannel.write("\n" + functionName + ",", 0);

    float totalTime = (endTime - startTime);
    float totalTimeMilli = (totalTime / 1000000.0);
    resultWrite = check charChannel.write(io:sprintf("%10.2f,", totalTimeMilli), 0);

    float avgLatency = (<float>totalTime / <float>benchmarkIterations);
    resultWrite = check charChannel.write(io:sprintf("%10.2f,", avgLatency), 0);

    float tps = (1000000000.0 / avgLatency);
    resultWrite = check charChannel.write(io:sprintf("%10.2f", tps), 0);
    var result = channel.close();
}

public function untaintedReturn(string input) returns @untainted string {
    return input;
}

