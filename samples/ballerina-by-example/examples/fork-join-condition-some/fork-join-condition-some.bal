import ballerina.lang.system;
import ballerina.doc;

@doc:Description { value:"fork-join condition allows users to wait desired number of workers to join before executing the join block"}
function main(string[] args) {

  // Declare the fork-join statement
  fork {
    worker W1 {
      // Define an integer variable within the worker to send to join block
      int i = 23;
      // Define a string variable within the worker to send to join block
      string n = "Colombo";
      // Print the values defined within worker W1
      system:println("[W1 worker]: inside worker
      Value of integer variable is [" + i + "]
      Value of string variable is [" + n + "]");
      // Send data to join block of the fork-join from worker W1
      i, n -> fork;
    }

    worker W2 {
      // Define a float variable within the worker to send to join block
      float f = 10.344;
      // Print the value defined within worker W2
      system:println("[W2 worker]: starting worker
      Value of float variable is [" + f + "]");
      // Send data to join block of the fork-join from worker W2
      f -> fork;
    }
  } join (some 1) (map results){
      // In the above join block, "some 1" is given to wait for one worker.

      // check whether the returned worker is W1
      if (results["W1"] != null) {
      any[] r1;
      // Values received from worker W1 are assigned to any array of r1
      r1,_   = (any[]) results["W1"];
      // Getting the 0th index of array returned from worker W1
      int p;
      p, _ = (int) r1[0];
      // Getting the 1th index of array returned from worker W1
      string l;
      l, _ = (string) r1[1];
      // Print values received from worker W1 within join block
      system:println("[default worker] within join:
      Value of integer from W1 is [" + p + "]");
      system:println("[default worker] within join:
      Value of string from W1 is [" + l + "]");
      }

      // check whether the returned worker is W2
      if (results["W2"] != null) {
      any[] r2;
      // Values received from worker W2 are assigned to any array of r2
      r2,_   = (any[]) results["W2"];
      // Getting the 0th index of array returned from worker W2
      float q;
      q, _ = (float) r2[0];
      // Print value received from worker W2 within join block
      system:println("[default worker] within join:
      Value of float from W2 [" + q + "]");
      }
  }

}
