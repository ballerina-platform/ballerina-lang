import ballerina.lang.system;
import ballerina.doc;

@doc:Description { value:"Worker interactions in ballerina allows users to share data across multiple workers."}
function main (string[] args) {
  // Some code within the default (main) worker
  int i = 100;
  float k = 2.34;
  // Print within the default (main) worker before sending data to worker W1
  system:println("[default worker] 
    Sending data to W1:
    Value of integer variable is [" + i + "]
    Value of float variable is [" + k + "]");
  // Send data to worker W1
  i, k -> W1;
  // Define variable to receive data from worker W1
  json j = {};
  // Receive data from worker W1
  j <- W1;
  // Print data received from worker W1
  system:print("[default worker]
    Data received from W1 worker:
    Value of json variable is ");
  system:println(j);


  // Define the worker and it's execution logic.
  worker W1 {
    // Define variables to receive data from default worker
    int iw;
    float kw;
    // Receive data from default worker
    iw, kw <- default;
    // Print values received from default worker
    system:println("[W1 worker]
    Data received from default worker:
    Value of integer variable is [" + iw + "]
    Value of float variable is [" + kw + "]");
    // Prepare data to send back to default worker
    json jw = {"name":"WSO2"};
    // Print before sending data to default worker
    system:print("[W1 worker]
    Sending data to default worker:
    Value of json variable is ");
    system:println(jw);
    // Send data to default worker
    jw -> default;
  }
}
