import ballerina/net.http;

function testCompositeConnector () (string, string) {
    TestLBConnector testLB;
    TestConnector t1 = create TestConnector("URI1");
    TestConnector t2 = create TestConnector("URI2");
    TestConnector[] tArray = [t1, t2];
    testLB = create TestLBConnector (tArray, "RoundRobin") with FilterConnector("Filter");
    http:Request request = {};
    string value1;
    string value2;
    value1 = testLB.action1(request);
    value2 = testLB.action1(request);
    return value1, value2;

}

connector FilterConnector<TestConnector t>(string param1) {

    action action1(http:Request request) (string){
          string x;
          x = t.action1(request);
          string y;
          y = t.action2(request);
          return x;
    }

    action action2( http:Request request) (string) {
          return "value within filter";
    }

}

connector TestConnector(string param1) {

    action action1(http:Request request) (string){
        return param1;
    }

    action action2(http:Request request) (string) {
        return "value from action2";
    }

}

connector TestLBConnector(TestConnector[] testConnectorArray, string algorithm) {

    int count = 0;

    action action1(http:Request request) (string){
        int index = count % lengthof testConnectorArray;
        TestConnector t1 = testConnectorArray[index];
        count = count + 1;
        string retValue = t1.action1(request);
        return "LB" + <string>count;
    }

    action action2(http:Request request) (string) {
        return "value from action2";
    }
}
