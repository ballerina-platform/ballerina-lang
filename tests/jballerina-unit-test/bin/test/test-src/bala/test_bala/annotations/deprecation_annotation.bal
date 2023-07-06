import testorg/foo;

public function func1(foo:DummyObject1 obj, foo:Bar b, string str = foo:C1) {
}

public function func2() {
    foo:DummyObject2 obj = new;
    obj.doThatOnObject("");
    int _ = obj.id;

    string _ = foo:deprecated_func();
    future<string> _ = start foo:deprecated_func();

    foo:DummyObject1 _ = new;
    foo:DummyObject1 _ = new ();
}

@foo:deprecatedAnnotation
function testAttachingDeprecatedAnnotation() {

}

function testAccessingDeprecatedAnnotation() {
    _ = (typeof testAttachingDeprecatedAnnotation).@foo:deprecatedAnnotation;
}

function testCallingDeprecatedRemoteMethod() {
    foo:MyClientObject clientObj = client object {
        remote function remoteFunction() {

        }
    };

    clientObj->remoteFunction();
}

function testUsageOfDeprecatedParam(@deprecated foo:C2 a) {
    _ = a;
}
