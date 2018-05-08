import ballerina/lang.system;
import ballerina/doc;

@doc:Description { value:"In scope variables can be accessed within workers of fork-join statement."}
function main(string... args) {
  // Define variables which are visible to the forked workers
  int i = 100;
  string s = "WSO2";
  map m = {"name":"Abhaya","era":"Anuradhapura"};

  // Declare the fork-join statement
  fork {
    worker W1 {
      // Change the value of the integer variable "i" within the worker W1
      i = 23;
      // Change the value of map variable "m" within the worker W1
      m["name"] = "Rajasinghe";
      // Define a variable within the worker to send to join block
      string n = "Colombo";
      // Send data to join block of the fork-join from worker W1
      i, n -> fork;
    }

    worker W2 {
      // Change the value of string variable "s" within the worker W2
      s = "Ballerina";
      // Change the value of map variable "m" within the worker W2
      m["era"] = "Kandy";
      // Send data to join block of the fork-join from worker W2
      s -> fork;
    }
  } join (all) (map results){
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
      string q;
      q, _ = (string) r2[0];
      // Print values received from workers within join block
      system:println("[default worker] within join:
      Value of integer from W1 is [" + p + "]");
      system:println("[default worker] within join:
      Value of string from W1 is [" + l + "]");
      system:println("[default worker] within join:
      Value of string from W2 [" + q + "]");
  }
  // Print values after the fork-join statement to check effect on variables.
  // Value type variables have not been changed since they are passed in as a
  // copy of the original variable
  system:println("[default worker] after fork-join:
      Value of integer variable is [" + i + "]
      Value of string variable is [" + s + "]");
  // Reference type variables are changed since they have passed in as a
  // reference to the workers
  string name;
  string era;
  name, _ =  (string)m["name"];
  era, _ = (string)m["era"];
  system:println("[default worker] after fork-join:
      Value of name is [" + name + "]
      Value of era is [" + era + "]");

}
