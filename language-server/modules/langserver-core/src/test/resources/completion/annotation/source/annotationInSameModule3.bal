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

type T2 object {
    string name = "ballerina";

    @
    public function setName(@v6 { foo: "v61 value required" } string name,
                            @v6 { foo: "v61 value defaultable" } int id = 0,
                            @v6 { foo: "v61 value rest" } string... others) returns @v7 () {
        self.name = name;
    }
};