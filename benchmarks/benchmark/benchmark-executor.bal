package benchmark;

import ballerina/io;
import ballerina/time;
import ballerina/file;

public type functionRecord {
    string functionName;
    function () f;
};

function main(string... args) {

    if (lengthof args < 3) {
        io:println("ERROR: Please specify the number of warm-up iterations and benchmark iterations.");
        return;
    }

    var warmupIterations = check <int>args[0];
    var benchmarkIterations = check <int>args[1];
    string resultFileName = untaintedReturn(<string>args[2]);

    io:println("Starting");
    // executeBenchmarks(getForkJoinBenchmarkArray(), warmupIterations, benchmarkIterations, resultFileName);
    executeBenchmarks(getTypeBenchmarkArray(), warmupIterations, benchmarkIterations, resultFileName);
    io:println("\nTests End ");
}

function executeBenchmarks(functionRecord[] functionArray, int warmupIterations,
                            int benchmarkIterations, string resultsFileName) {

    // create results folder
    string resultsFolderName = "results";
    file:Path dirPath = new(resultsFolderName);

    if (!file:exists(dirPath)){
        var dir = file:createDirectory(dirPath);
    }

    // write ReadMe
    string fileReadMeLocation = resultsFolderName + "/" + resultsFileName + ".txt";
    io:ByteChannel channelR = io:openFile(fileReadMeLocation, "W");
    io:CharacterChannel charChannelR = check io:createCharacterChannel(channelR, "UTF-8");
    int resultWriteR = check charChannelR.writeCharacters("This test carried out with warmupIterations of " +
            warmupIterations + " and benchmarkIterations of " + benchmarkIterations + ".", 0);

    // write results file
    string resultsFileLocation = resultsFolderName + "/" + resultsFileName + ".csv";
    io:ByteChannel channel = io:openFile(resultsFileLocation, "W");
    io:CharacterChannel charChannel = check io:createCharacterChannel(channel, "UTF-8");
    int resultWrite = check charChannel.writeCharacters("Function_Name,Total Time (ms),Average Latency (ns),Throughput (operations/second) ", 0);

    foreach key, value in functionArray {

        int i = 0;
        var temp = value;
        int warmupstartTime = time:nanoTime();

        while (i < warmupIterations) {
            i = i + 1;
            function () f = value.f;
            f();
        }

        // for debugging purpose
        io:println(value.functionName);

        i = 0;
        int startTime = time:nanoTime();

        while (i < benchmarkIterations) {
            i = i + 1;
            function () f = value.f;
            f();
        }

        int endTime = time:nanoTime();

        resultWrite = check charChannel.writeCharacters("\n" + value.functionName + ",", 0);

        float totalTime = (endTime - startTime);
        float totalTimeMilli = (totalTime / 1000000.0);
        resultWrite = check charChannel.writeCharacters(io:sprintf("%10.2f,", totalTimeMilli), 0);

        float avgLatency = (<float>totalTime / <float>benchmarkIterations);
        resultWrite = check charChannel.writeCharacters(io:sprintf("%10.2f,", avgLatency), 0);

        float tps = (1000000000.0 / avgLatency);
        resultWrite = check charChannel.writeCharacters(io:sprintf("%10.2f", tps), 0);
    }

}

public function untaintedReturn(string input) returns @untainted string {
    return input;
}

