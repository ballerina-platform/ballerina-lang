import ballerina/net.http;

const string myConst = "MyParam1";

connector TestConnector(string param1, string param2, int param3) {

    endpoint<EchoConnector> echoConnector {
        create EchoConnector(param1);
    }

    boolean action2Invoked;

    action action1() (boolean){
        return action2Invoked;
    }

    action action2() {
        action2Invoked = true;
    }

    action action3() (string) {
        return param1;
    }

    action action4(string actionParam) (string, string, int) {
        return actionParam, param2, param3;
    }

    action action5(string actionParam) (string) {
        string s;
        s = echoConnector.echoAction(actionParam);
        return s;
    }

    action action6(string echoConnectorParam, string actionParam) (string) {
        endpoint<EchoConnector> localEchoConnector {
            create EchoConnector(echoConnectorParam);
        }
        string s;

        s =  localEchoConnector.echoAction(actionParam);
        return s;
    }
}

connector EchoConnector(string greeting) {

    action echoAction(string name) (string) {
        return greeting + ", " + name;
    }

}

@http:configuration {
    basePath:"/invoke"
}
service<http> actionInvokeService {

    endpoint<TestConnector> testConnector {
        create TestConnector(myConst, "MyParam2", 5);
    }

    @http:resourceConfig {
        methods:["GET"],
        path : "/action3"
    }
    resource action3Resource (http:Connection conn, http:Request req) {

        string actionResponse;
        actionResponse = testConnector.action3();
        http:Response res = {};
        res.setStringPayload(actionResponse);
        _ = conn.respond(res);
    }

    
    @http:resourceConfig {
        methods:["GET"],
        path : "/action1"
    }
    resource action1Resource (http:Connection conn, http:Request req) {

        boolean actionResponse;
        actionResponse = testConnector.action1();
        string payload = <string> actionResponse;
        http:Response res = {};
        res.setStringPayload(payload);
        _ = conn.respond(res);
    }
    
    @http:resourceConfig {
        methods:["GET"],
        path : "/action2"
    }
    resource action2Resource (http:Connection conn, http:Request req) {

        http:Response res = {};
        testConnector.action2();
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path : "/action5"
    }
    resource action5Resource (http:Connection conn, http:Request req) {

        string actionResponse;
        actionResponse = testConnector.action5(myConst);
        http:Response res = {};
        res.setStringPayload(actionResponse);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path : "/action6"
    }
    resource action6Resource (http:Connection conn, http:Request req) {

        string actionResponse;
        actionResponse = testConnector.action6("Hello", "World");
        http:Response res = {};
        res.setStringPayload(actionResponse);
        _ = conn.respond(res);
    }
}