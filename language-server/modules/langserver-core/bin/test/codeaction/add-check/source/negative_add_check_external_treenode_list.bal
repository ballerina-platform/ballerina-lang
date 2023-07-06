type Foo object {
    string name;
};

type A object {
    *Foo;
    int z;
};

A x = object {123, ""}
