import ballerina/module1;

type AnnotationData record {|
    int field1;
|};

annotation AnnotationData MyAnnotation on service;

public type MyService service object {
    
};


@MyAnnotation {
    field1: 0
}
service MyService /path on new module1:Listener(9090) {
    resource function get .() returns string {
        getServiceAuthConfig(self, "foo");
        return "Hello, World!";
    }
}

isolated function getServiceAuthConfig(MyService serviceRef, string servicePath) {
    typedesc<service object{}> serviceTypeDesc = typeof serviceRef;
    int field1 = (<AnnotationData>serviceTypeDesc.@MyAnnotation).;
}
