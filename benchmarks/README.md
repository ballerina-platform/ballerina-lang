# Ballerina-benchmarks

This module includes micro-benchmarks.

##### To run benchmarks: 
 `mvn clean install -P benchmarks -Dwarmup.iterations=<number_of_warm_up_iterations> -Dbenchmark.iterations=<number_of_benchmark_iterations>`

eg:- `mvn clean install -P benchmarks -Dwarmup.iterations=22 -Dbenchmark.iterations=25000`

##### Results
The benchmark results will be created in results folder ( <Project_Home>/benchmarks/results ) in CSV file 
format with name benchmark-ballerina_${project.version}.
