function testInvalidNumberedLiterals () {
    (float, int, string) x = (1.2, 3);
}

function testInvalidIndexAccess () {
    (float, int, string) x = (1.2, 3, "abc");
    any x1 = x[-1];
    any x2 = x[3];
    int index = 0;
    float x3 = x[index];
    any x4 = x["0"];
}
