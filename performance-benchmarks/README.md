# Ballerina-performance-benchmarks

To run the performance benchmarks:

mvn clean install -P benchmark -Dwarmup.iterations=<No_of_warmUp_Iterations> -Dbenchmark.iterations=<No_of_benchmark_Iterations>

eg:- mvn clean install -P benchmark -Dwarmup.iterations=22 -Dbenchmark.iterations=25000

Results will be created in results folder with the project.version identifier.
