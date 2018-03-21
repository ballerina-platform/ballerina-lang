import ballerina/net.http;
import samples.ballerina-

function testCompositeConnector()(string, string) {
    TestLBConnector testLB;
    TestConnector t1 = create TestConnector("URI1") with FilterConnector("XXXX");
    TestConnector t2 = create TestConnector("URI2") with FilterConnector2("YYYY");
    TestConnector[] tArray = [t1, t2];
    testLB = create TestLBConnector (tArray, "RoundRobin");
    http:Request request = {};
    string value1;
    string value2;
    value1 = testLB.action1(request);
    value2 = testLB.action1(request);
    return value1, value2;

}

connector TestConnector(string param1) {

    action action1(http:Request request) (string){
        return param1;
    }

    action action2(http:Request request) (string) {
        return "value from action2";
    }

}

connector FilterConnector<TestConnector t>(string param1) {

    action action1(http:Request request) (string){
          string x;
          x = t.action1(request);
          string y;
          y = t.action2(request);
          return param1;
    }

    action action2(http:Request request) (string) {
          return "value within filter";
    }

}

connector FilterConnector2<TestConnector t>(string param1) {

    action action1(http:Request request) (string){
          string x;
          x = t.action1(request);
          string y;
          y = t.action2(request);
          return "2222222";
    }

    action action2(http:Request request) (string) {
          return "value within filter";
    }

}

connector TestLBConnector(TestConnector[] testConnectorArray, string algorithm) {

    int count = 0;

    action action1(http:Request request) (string){
        int index = count % lengthof testConnectorArray;
        TestConnector t1 = testConnectorArray[index];
        count = count + 1;
        string retValue = t1.action1(request);
        return retValue;
    }

    action action2(http:Request request) (string) {
        return "value from action2";
    }
}