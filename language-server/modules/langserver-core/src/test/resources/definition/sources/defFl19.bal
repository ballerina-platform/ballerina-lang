import ballerina/http;
import ballerina/lang.'object as lang;
import ballerina/reflect;

type Annot record {
    string foo;  
    int bar?;
};

public annotation map<int> v1 on function;
annotation v2 on parameter;
public annotation Annot[] v3 on return;
annotation Annot v4 on service;
annotation Annot v5 on class;
const annotation map<string> v6 on source annotation;

@v6 {
    str: "v6 value"
}
const annotation map<string> v7 on source annotation;

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
    *lang:Listener;

    public function init() {
    }

    public function __attach(service s, string? name = ()) returns error? {
    }

    public function __start() returns error? {
    }

    public function __gracefulStop() returns error? {
        return ();
    }

    public function __immediateStop() returns error? {
        return ();
    }
};

function testServiceAnnotAccess() returns boolean {
    Annot? annot = <Annot?> reflect:getServiceAnnotations(ser, "v4");
    if (annot is Annot) {
        return annot.foo == v4a;
    }
    return false;
}

@v5 {
    foo: "v5 value"
}
type T2 object {
    string name = "ballerina";
};

function annotationAccessExpressionTest() {
    service ser = @v4 {
        foo: v4a
    }service {
        resource function res() {
        }
    };

    typedesc<service> td = typeof ser;
    Annot? m1 = td.@v4;
}