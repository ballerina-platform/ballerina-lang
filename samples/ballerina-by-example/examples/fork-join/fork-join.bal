import ballerina.lang.system;
import ballerina.doc;

@doc:Description { value:"fork-join functionality in ballerina allows users to spawn(fork) multiple workers within a ballerina program and join the results from those workers and execute code on joined results."}
function main(string[] args) {

  // Declare the fork-join statement
  fork {
    worker W1 {
      // Define an integer variable within the worker to send to join block
      int i = 23;
      // Define a string variable within the worker to send to join block
      string n = "Colombo";
      // Print the values of variables visible to worker W2
      system:println("[W1 worker]: inside worker
      Value of integer variable is [" + i + "]
      Value of string variable is [" + n + "]");
      // Send data to join block of the fork-join from worker W1
      i, n -> fork;
    }

    worker W2 {
      // Define a float variable within the worker to send to join block
      float f = 10.344;
      // Print the values of variables within worker W2
      system:println("[W2 worker]: starting worker
      Value of float variable is [" + f + "]");
      // Send data to join block of the fork-join from worker W2
      f -> fork;
    }
  } join (all) (map results){
      // In the above join block, "all" is given to wait for all workers.
      // Declare variables to receive the results from forked workers W1 and W2
      any[] r1;
      any[] r2;
      // results map contains a map of any type array from each worker
      // defined within the fork-join statement
      // Values received from worker W1 are assigned to any array of r1
      r1,_   = (any[]) results["W1"];
      // Values received from worker W2 are assigned to any array of r2
      r2,_   = (any[]) results["W2"];
      // Getting the 0th index of array returned from worker W1
      int p;
      p, _ = (int) r1[0];
      // Getting the 1th index of array returned from worker W1
      string l;
      l, _ = (string) r1[1];
      // Getting the 0th index of array returned from worker W2
      float q;
      q, _ = (float) r2[0];
      // Print values received from workers within join block
      system:println("[default worker] within join:
      Value of integer from W1 is [" + p + "]");
      system:println("[default worker] within join:
      Value of string from W1 is [" + l + "]");
      system:println("[default worker] within join:
      Value of float from W2 [" + q + "]");
  }

}
