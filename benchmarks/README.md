# Ballerina-benchmarks

### How To Visualize benchmarks 

##### Prerequisite:
 Having the ballerina profile build.
 To build ballerina profile only `mvn clean install -P ballerina`

##### To run benchmarks: 
 `mvn clean install -P benchmarks -Dwarmup.iterations=<No_of_warmUp_Iterations> -Dbenchmark.iterations=<No_of_benchmark_Iterations>`

eg:- `mvn clean install -P benchmarks -Dwarmup.iterations=22 -Dbenchmark.iterations=25000`

##### Results
The benchmark results will be created in results folder ( <Project_Home>/benchmarks/results ) in CSV file 
format with name benchmark-ballerina_${project.version}.
