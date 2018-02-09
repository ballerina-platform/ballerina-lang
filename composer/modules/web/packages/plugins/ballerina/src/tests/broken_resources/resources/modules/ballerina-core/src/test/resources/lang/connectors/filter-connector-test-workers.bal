function testAction1() (json) {
    TestConnector testConnector = create TestConnector("MyParam1", "MyParam2", 5) with FilterConnector("MyTest1", "MyTest2", 15);
    message request;
    json value;

    request = {};
    value = TestConnector.action1(testConnector, request);
    return value;
}

connector TestConnector(string param1, string param2, int param3) {

    boolean action2Invoked;

    action action1(TestConnector testConnector, message msg) (json){
          float aa;
          json result;
          aa = 13;
          msg -> sampleWorker;
          result <- sampleWorker;
          return result;

          worker sampleWorker {
          float amount;
          float sumD;
          int quantity;
          float a;
          json j;
          message m;
          m <- default;
          j = {"name":"chanaka"};
          sumD = 123d;
          amount = 222d;
          quantity = 12;
          a = 123d;
          sumD = sumD + ( amount * quantity );
          j -> default;
    }
    }

    action action2(TestConnector testConnector, message msg) (json){
    json result;
    result <- sampleWorker;
    return result;

    worker sampleWorker {
        json j;
        j = {"name":"chanaka"};
        j -> default;
    }
    }

}


connector FilterConnector<TestConnector t> (string param1, string param2, int param3) {

    boolean action2Invoked;

    action action1(FilterConnector testConnector, message msg) (json) {
          float aa;
          json result;
          aa = 133;
          msg -> sampleWorker;
          result <- sampleWorker;
          json x;
          x = TestConnector.action1(t, msg);
          return x;

          worker sampleWorker {
          float amount;
          float sumD;
          int quantity;
          float a;
          json j;
          message m;
          m <- default;
          j = {"name":"chanaka"};
          sumD = 123d;
          amount = 222d;
          quantity = 12;
          a = 123d;
          sumD = sumD + ( amount * quantity );
          j -> default;
    }
    }

    action action2(FilterConnector testConnector, message msg) (json) {
    json result;
    result <- sampleWorker;
    return result;

    worker sampleWorker {
        json j;
        j = {"name":"chanaka"};
        j -> default;
    }
    }

}

