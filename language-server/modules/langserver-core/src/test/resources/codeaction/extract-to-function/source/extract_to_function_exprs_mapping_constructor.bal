public const annotation map<string> v1 on source type, class;
// following case fails because the mapping constructor node's typeSymbol is empty
@v1 {
    foo: "foo",
    bar: "bar"
}
public type Record record {
    string name;
};

Record moduleVar = { name: "ballerina" };

function testFunction() {
    Record localVar = { name: "ballerina" };

    map<int> map1 = {"Maths": 90, "Science": 85};
}
