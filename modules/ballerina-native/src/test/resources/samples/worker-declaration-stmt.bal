import ballerina.lang.system;
import ballerina.lang.messages;

function testworker(message msg)(message) {
  float aa;
  message result;
  int count = 1000;
  //message msg = {};
  aa = 13;
  system:println(aa);
  count, aa, msg -> sampleWorker;
  system:println("Worker in function test started");
  result <- sampleWorker;
  string s = messages:getStringPayload(result);
  aa -> sampleWorker3;
  system:println(s);
  result <- sampleWorker3;
  return result;

  worker sampleWorker  {
  float amount;
  float sumD;
  int quantity;
  float a;
  json j;
  message m;
  message m2;
  quantity, a, m <- default;
  quantity, a, m -> sampleWorker2;
  j = `{"name":"chanaka"}`;
  messages:setJsonPayload(m, j);
  sumD = 123d;
  amount = 222d;
  //quantity = 12;
  //a = 123d;
  sumD = sumD + ( amount * quantity );
  system:println(a);
  system:println(quantity);
  m -> default;
  m2 <- sampleWorker2;
  system:println(m2);

  worker sampleWorker2 {
  int quantity;
  float a;
  json j;
  message m;
  quantity, a, m <- sampleWorker;
  j = `{"name":"IBM"}`;
  messages:setJsonPayload(m, j);
  system:println("XXXXXXXXXXXXXXX");
  system:println(a);
  system:println(quantity);
  m -> sampleWorker;
  }

  }

  worker sampleWorker3 {
  float index;
  json j;
  message m = {};
  index <- default;
  j = `{"name":"WSO2"}`;
  messages:setJsonPayload(m, j);
  system:println("YYYYYYYYYYYYYY");
  system:println(index);
  m -> default;
  }

}

function testSimpleWorker(message msg)(message) {
    message result;
    //message msg = {};
    int x = 100;
    float y;
    map p = { "a" : 1, "b" : 2};
    p, msg, x ->sampleWorker;
    system:println("Worker calling function test started");
    y, result <-sampleWorker;
    string s = messages:getStringPayload(result);
    system:println(s);
    system:println("Value received from worker is " + (int)p["a"]);
    return result;

    worker sampleWorker {
    message result;
    message m;
    int a;
    map q;
    float b = 12.34;
    q, m, a <-default;
    q["a"] = 12;
    system:println("passed in value is " + (int)q["a"]);
    json j;
    j = `{"name":"chanaka"}`;
    messages:setJsonPayload(m, j);
    b, m ->default;
}

}

function testAccessFunctionArgumentsWorker(message msg)(message) {
    message result;
    int x = 100;
    float y;
    map p = { "a" : 1, "b" : 2};
    p, x ->sampleWorker;
    system:println("Worker calling function test started");
    y, result <-sampleWorker;
    string s = messages:getStringPayload(result);
    system:println(s);
    system:println("Value received from worker is " + (int)p["a"]);
    return result;

    worker sampleWorker {
    message result;
    message m;
    int a;
    map q;
    float b = 12.34;
    q, a <-default;
    q["a"] = 12;
    system:println("passed in value is " + (int)q["a"]);
    json j;
    j = `{"name":"chanaka"}`;
    messages:setJsonPayload(msg, j);
    b, msg ->default;
}

}




