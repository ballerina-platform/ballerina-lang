type Test1 record {
    string name;
    int age;
};

type Test2 record {
    Test1 person;
    string name;
};

public function main() {
    Test2 p = {};
}
