import ballerina/http;
import ballerina/reflect;

type Annot record {
    string foo;  
    int bar?;
};

public annotation map<int> v1 on function;
annotation v2 on parameter;
public annotation Annot[] v3 on return;
annotation Annot v4 on service;

listener Listener lis = new;

string v4a = "v4a";

@v4 {
    foo: v4a
}
service ser on lis {

    @v1 {
        first: 1,
        second: 2
    }
    @http:ResourceConfig {
        path: "testPath"
    }
    resource function res(@v2 int intVal, string strVal) returns
                            @v3 { foo: "v41" }  @v3 { foo: "v42", bar: 2 } string {
        return "";
    }
}

type Listener object {
    *AbstractListener;

    public function __init() {
    }

    public function __attach(service s, string? name = ()) returns error? {
    }

    public function __start() returns error? {
    }

    public function __stop() returns error? {
    }
};

function testServiceAnnotAccess() returns boolean {
    Annot? annot = <Annot?> reflect:getServiceAnnotations(ser, "v4");
    if (annot is Annot) {
        return annot.foo == v4a;
    }
    return false;
}