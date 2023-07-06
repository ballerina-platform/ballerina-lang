import ballerina/module1;

public function testStdlibGotoDefinition() {
     module1:Client cl = new("http://loaclhost:9090");
     module1:function4(12, 34);
     var response = cl->post("/test", "Hello World");
}

public function testLangLig() {
    string[] stringArr = [];
    int length = stringArr.length();
}

public function testStdlibObjectFieldDefinition() {
    module1:TestObject1 objectVar = new(12, 34);
    objectVar.field1 = 1234;
}
