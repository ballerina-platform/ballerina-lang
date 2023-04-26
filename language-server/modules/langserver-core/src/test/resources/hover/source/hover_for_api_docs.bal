import ballerina/module1;
import ballerina/lang.value as vl;
//function
int min = int:max(10, 20, 30);

//client
module1:Client cli = new module1:Client("");

//listener
module1:Listener mod1Listener = new module1:Listener(9090);

public function main() {
    //class
    module1:TestClass1 testClass;

    //objectType
    module1:TestObject1 obj;

    //Record
    module1:TestRecord1 rec;

    //Enum
    module1:TestEnum1 testEnum;

    //Constant
    int constant = module1:TEST_INT_CONST1;

    //types
    module1:TestMap2 testMap;

    //Variables
    var con = module1:GLOBAL_VAR;

    //Errors
    module1:ErrorOne err;
}

@module1:functionAnnotation1 {
    foo:"foo"
}
function testAnnotation() {
    
}

public function testModAlias() {
    string balStrinst = vl:toBalString(10);
}
