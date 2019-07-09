function testIntAssignStmt(int a) returns int {
    // primitive assignment
    int x;
    x = a;

    // array field assignment
    int[] arr = [];
    arr[0] = a;

    int a2;
    string name;
    int b;

    // tuple assignment
    [a2, name, b] = [1, "Bob", a];

    // cast and assign
    float f;
    f = <float>a;

    return 0;
}

function testConstrainedMapWithRecordInTupleWithoutType() {
    RecData1 d1 = { i: 1, v: "A" };
    [int, RecData1] t1 = [2, d1];
    map<[int, RecData1]> m = { a: t1 };
}

type RecData1 record {
    int i;
    string v;
};