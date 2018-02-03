function testCompositeConnector()(string, string) {
    TestConnector t1 = create TestConnector("URI1") with FilterConnector("XXXX");
    TestConnector t2 = create TestConnector("URI2") with FilterConnector("YYYY");
    TestConnector[] tArray = [t1, t2];
    TestLBConnector testLB = create TestLBConnector (tArray, "RoundRobin");
    message request = {};
    string value1;
    string value2;
    value1 = TestLBConnector.action1(testLB, request);
    value2 = TestLBConnector.action1(testLB, request);
    return value1, value2;

}

connector TestConnector(string param1) {

    action action1(TestConnector testConnector, message msg) (string){
        return param1;
    }

    action action2(TestConnector testConnector, message msg) (string) {
        return "value from action2";
    }

}

connector FilterConnector<TestConnector t>(string param1) {

    action action1(FilterConnector testConnector, message msg) (string){
          string x;
          x = TestConnector.action1(t, msg);
          string y;
          y = TestConnector.action2(t, msg);
          return param1;
    }

    action action2(FilterConnector testConnector, message msg) (string) {
          return "value within filter";
    }

}

connector FilterConnector2<TestConnector t>(string param1) {

    action action1(FilterConnector2 testConnector, message msg) (string){
          string x;
          x = TestConnector.action1(t, msg);
          string y;
          y = TestConnector.action2(t, msg);
          return "2222222";
    }

    action action2(FilterConnector2 testConnector, message msg) (string) {
          return "value within filter";
    }

}

connector TestLBConnector(TestConnector[] testConnectorArray, string algorithm) {

    int count = 0;

    action action1(TestLBConnector testConnector, message msg) (string){
        int index = count % testConnectorArray.length;
        TestConnector t1 = testConnectorArray[index];
        count = count + 1;
        string retValue = TestConnector.action1(t1, msg);
        return retValue;
    }

    action action2(TestLBConnector testConnector, message msg) (string) {
        return "value from action2";
    }
}
