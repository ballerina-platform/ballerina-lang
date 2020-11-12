# Ballerina-Lang Feature Benchmarks

This module includes micro benchmarks for language related features.

### Writing tests:

There are two types of tests, namely, single execution benchmark tests and multi execution benchmark 
tests. Multi execution benchmark will get executed (`warmupCount` + `benchmarkCount`) times automatically and will get 
benchmarked. However, single execution benchmarks will get executed once and the user is responsible for handling 
warm-up and benchmarking within the test function itself (i.e. testing loops).

##### Writing single execution benchmark test
```ballerina
    function singleExecutionBenchmarkTest(int warmupCount, int benchmarkCount) returns int {
        // code to iterate `warmupCount` times & warm-up.
        int startTime = nanoTime();
        // code to iterate `benchmarkCount` times & benchmark.
        // return total time taken in nano seconds.
        return (nanoTime() - startTime);
    }
```
1. Write a benchmark function similar to above.
2. Handle warm-up and benchmark iterations within the function.
3. Register benchmark function on `registerSingleExecFunctions()`.
4. Add function name to `benchmarkFunctions.txt`.

##### Writing multi execution benchmark test
```ballerina
    function multiExecutionBenchmarkTest() {
        // code to benchmark.
    }
```
1. Write a benchmark function similar to above.
2. Register benchmark function on `registerMultiExecFunctions()`.
3. Add function name to `benchmarkFunctions.txt`.

### Running benchmarks: 
command:- 
```
./gradlew :jballerina-benchmark-test:test
```

supported args:-
```
    -Pwarmup.iterations=<number_of_warm_up_iterations>
    -Pbenchmark.iterations=<number_of_benchmark_iterations>
    -Pballerina.home=<ballerina_home_to_run_tests_against>
    -Presults.location=<results_file_location>
    -Pgc.logs.location=<gc_logs_location>
    -Pgcviewer.jar=<gcviewer_jar_location>
```

### Results
The benchmark results will be created in results folder (`<Project_Home>/jballerina-benchmark-test/build/results`) in 
CSV file format with name `benchmark_ballerina_${project.version}.csv`.
