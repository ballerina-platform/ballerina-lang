package lang.errors.connectors;

import lang.errors.runtime;

@http:BasePath ("/test")
service echoService {
    @http:GET
    @http:Path ("/connector")
    resource echoResource (message m) {
		TestConnector testConnector = create TestConnector("MyParam1", "MyParam2", 5);
    	boolean value = TestConnector.action1(testConnector);
        reply m;
    }
}

connector TestConnector(string param1, string param2, int param3) {
    action action1(TestConnector testConnector) (boolean){
    	runtime:testStackTrace();
        return true;
    }
}
