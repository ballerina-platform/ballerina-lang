import ballerina/net.http;

function testCompositeConnector () (string, string) {
    endpoint<TestLBConnector> ep {

    }
    TestLBConnector testLB;
    TestConnector t1 = create TestConnector("URI1");
    TestConnector t2 = create TestConnector("URI2");
    TestConnector[] tArray = [t1, t2];
    testLB = create TestLBConnector(tArray, "RoundRobin");
    http:Request req = {};
    string value1;
    string value2;
    bind testLB with ep;
    value1 = ep.action1(req);
    value2 = ep.action1(req);
    return value1, value2;

}

connector TestConnector(string param1) {

    action action1(http:Request req) (string) {
        return param1;
    }

    action action2(http:Request req) (string) {
        return "value from action2";
    }

}

connector TestLBConnector(TestConnector[] testConnectorArray, string algorithm) {

    int count = 0;

    action action1(http:Request req) (string) {
        endpoint<TestConnector> t1 {

        }
        int index = count % lengthof testConnectorArray;
        bind testConnectorArray[index] with t1;
        count = count + 1;
        string retValue = t1.action1(req);
        return retValue;
    }

    action action2(http:Request req) (string) {
        return "value from action2";
    }
}
