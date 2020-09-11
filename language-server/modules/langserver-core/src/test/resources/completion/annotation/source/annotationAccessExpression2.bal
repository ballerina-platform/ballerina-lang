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

@v1 {
    foo: strValue,
    bar: 1
}
public type T1 record {
    string name;
};

T1 a = { name: "John" };

function testAnnotationAccessExpression() {
	typedesc<T1> t = typeof a;
    any annotationVal = a.@h
}
