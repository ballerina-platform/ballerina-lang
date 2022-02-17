function testFunction() {
    int|error val =10;
    check val.ensureType(error);
}
