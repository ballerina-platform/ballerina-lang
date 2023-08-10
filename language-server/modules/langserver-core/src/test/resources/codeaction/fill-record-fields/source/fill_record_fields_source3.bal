type Record1 record {
    Record2 rec1f1;
    int rec1f2;
};

type Record2 record {
    int rec2f1;
    int rec2f2;
};

function testFunction() returns Record1 {
    return {};
}
