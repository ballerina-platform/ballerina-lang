type Family record {
    string spouse;
    int noOfChildren;
    string[] children;
};

type Foo record {
    int x;
    string y;
};

function testDuplicatedKey() {
    Family family = {spouse: "Shereen", noOfChildren: 1, noOfChildren: 0, children: []}; // invalid usage of record literal: duplicate key 'noOfChildren'
    Foo m1 = { x: 1, y: "B", "z": "rest", x: 12 }; // invalid usage of record literal: duplicate key 'x'
    Foo m2 = { x: 1, y: "B", "z": "rest", "a": "rest2", x: 12 }; // invalid usage of record literal: duplicate key 'x'
}
