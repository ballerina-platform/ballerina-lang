public class Foo {
    string field1 = "abc";

}

public class Bar {
    string name1 = "";
    private int n;

    public function init(int n = 0) {
        self.n = n;
    }

    public function inc() {
        self.n += 1;
        int x = 5;

    }
}

type Person record {|
    string name2;

|};

type PersonObj object {
    string name3;

};
