type Foo record {
    int id;
};

type Bar record {
    string name;
};

function myfunc() returns [Foo[], Bar]? {

}

public function test() {
    [Foo[], Bar]? res = myfunc();
}
