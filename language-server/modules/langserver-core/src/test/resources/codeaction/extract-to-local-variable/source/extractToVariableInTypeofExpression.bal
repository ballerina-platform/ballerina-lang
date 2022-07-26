public type T1 record {
    string name;
};

T1 a = { name: "John" };

function testFunction() {
    typedesc<T1> t2 = typeof a;
}