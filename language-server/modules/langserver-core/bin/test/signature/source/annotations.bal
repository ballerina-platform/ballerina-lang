public type Child object {
    # Returns foo
    # + a - float
    # + b - boolean
    public function foo(float a, boolean b) {

    }
};

type Annot record {
    Child foo;
    Child bar?;
};

public annotation Annot v1 on type;
@v1 {
    foo: bar2(1, false),
    bar: bar2(1, false)
}
public type T1 record {
    string name;
};

T1 a = { name: "John" };

function testAnnotationAccessExpression() {
	typedesc<T1> t = typeof a;
    Annot? annotationVal = t.@v1;
    if(annotationVal is Annot) {
        annotationVal.foo.foo(1, false);
    }
}

# Returns bar
# + a - float
# + b - boolean
# + return - child
public function bar2(float a, boolean b) returns Child {
    return new Child();
}
