import ballerina/module1;

service testService on new module1:Listener(8080) {
	resource function testResource(module1:TestRecord2 params) {
	    module1:TestRecord1 response = self.testServiceFunction();	
	}
}