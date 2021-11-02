import testorg/foo;

public function func1(foo:DummyObject1 obj, foo:Bar b, string str = foo:C1) {
}

public function func2() {
    foo:DummyObject2 obj = new;
    obj.doThatOnObject("");
    string _ = foo:deprecated_func();
}