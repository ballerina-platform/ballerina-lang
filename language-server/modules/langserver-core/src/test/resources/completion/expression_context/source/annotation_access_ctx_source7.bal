import ballerina/module1;

type AnnotationData record {|

|};
annotation AnnotationData MyAnnotation on service;

public type MyService service object {

};

@MyAnnotation
service MyService /path on new module1:Listener(9090) {
    resource function get .() returns string {
        getServiceAuthConfig(self, "foo");
        return "Hello, World!";
    }
}

isolated function getServiceAuthConfig(MyService serviceRef, string servicePath) {
    typedesc<any> serviceTypeDesc = typeof serviceRef;
    AnnotationData serviceAnnotation = <AnnotationData>serviceTypeDesc.@
}
