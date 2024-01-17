function testConditional1(int arg) {
    string val = arg > 0 ? getString(arg) : "";
}

function testConditional2(int arg) {
    arg > 0 ? getString(arg) : "";
}

function testConditional3(int arg) {
    arg > 0 ? "" : getString(arg);
}
