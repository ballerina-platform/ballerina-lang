@annot1 {}
public function foo() {
}

@annot2 {}
type foo record {

    @annot3 {}
    int a;

    @annot4 {}
    int b = 3;
};

@annot5 {}
type foo object {

    @annot6 {}
    int a;

    @annot7 {}
    public function bar();
};

@annot8 {}
service foo on bar {

    @annot9 {}
    remote function getName() {
    }

    @annot10 {}
    function getName() {
    }

    @annot11 {}
    resource function get Name() {
    }
}

@annot12 {}
const a = "hello";

@annot13 {}
int b = 4;

@annot14 {}
listener string c = "http://ballerinalang.io";
