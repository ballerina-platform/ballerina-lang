type Foo record {
    int id;
};

type Bar record {
    string name;
};

function myfunc() returns Foo|[Foo[], Bar] {

}

public function main() {
    Foo|[Foo[], Bar] f = myfunc();
}
