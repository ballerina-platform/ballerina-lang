import ballerina.lang.system;
import ballerina.doc;

@doc:Description { value:"Workers in ballerina allows users to delegate tasks to a new worker thread."}
function main (string[] args) {
  // Define variables within the default (main) worker
  int i = 100;
  float k = 2.34;
  // Print values within the default (main) worker
  system:println("[default worker]
    Value of integer variable is [" + i + "]
    Value of float variable is [" + k + "]");

  // Define the worker and it's execution logic.
  worker W1 {
    // Define variables within the W1 worker
    int iw = 200;
    float kw = 5.44;
    // Print values within the W1 worker
    system:println("[W1 worker]
    Value of integer variable is [" + iw + "]
    Value of float variable is [" + kw + "]");
  }
}
