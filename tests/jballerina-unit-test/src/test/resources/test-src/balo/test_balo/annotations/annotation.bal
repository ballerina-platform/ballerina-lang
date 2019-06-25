import testorg/foo version v1;
import ballerina/reflect;
import ballerina/http;


@foo:ConfigAnnotation {
    numVal: 10,
    textVal: "text",
    conditionVal: false,
    recordVal: { nestNumVal: 20, nextTextVal: "nestText" }
}
function someFunction(string arg) returns int {
    return 10;
}

function testNonBallerinaAnnotations() returns reflect:annotationData[] {
    return reflect:getFunctionAnnotations(someFunction);
}

@http:ServiceConfig {
    basePath: "/myService"
}
service MyService on new http:MockListener(9090) {

    @http:ResourceConfig {
        path: "/bar"
    }
    resource function foo(http:Caller caller, http:Request req) {

    }
}

function testBallerinaServiceAnnotations() returns reflect:annotationData[] {
    return reflect:getServiceAnnotations(MyService);
}

function testBallerinaResourceAnnotations() returns reflect:annotationData[] {
    return reflect:getResourceAnnotations(MyService, "foo");
}
