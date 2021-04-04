type Annot record {
    int foo;
};

public annotation Annot v1 on type;
@v1 {
    foo: 1
}
public type T1 record {
    string name;
};

T1 a = { name: "John" };

function testAnnotationAccessExpression() {
	typedesc<T1> t = typeof a;
    int annotationVal = t.@v1.
}