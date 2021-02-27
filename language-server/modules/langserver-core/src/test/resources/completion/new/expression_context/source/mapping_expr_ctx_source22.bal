type Record1 record {
    Record2 rec1f1;
    int rec1f2;
};

type Record2 record {
    int rec2f1;
    int rec2f2;
};

function name2() returns record {int field1; int field2; Record2 field3} {
    return {
        field3: {f}
    }
}