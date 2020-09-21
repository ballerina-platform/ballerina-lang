import ballerina/lang.'object as lang;

type Annot record {
    string foo;
    int bar?;
};

public annotation Annot v1 on type;
annotation Annot[] v2 on class;
public annotation Annot v3 on function;
annotation map<int> v4 on object function;
public annotation map<string> v5 on resource function;
annotation Annot v6 on parameter;
public annotation v7 on return;
annotation Annot[] v8 on service;

string strValue = "v1 value";

listener Listener lis = new;

string v8a = "v8a";

@
service ser on lis {

    resource function res(@v6 { foo: "v64" } int intVal) returns @v7 string {
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