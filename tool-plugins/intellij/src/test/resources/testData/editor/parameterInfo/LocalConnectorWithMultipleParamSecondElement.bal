function main (string... args) {
    test t = create test("a", a<caret>);
}

connector test (string a, string b) {

}
