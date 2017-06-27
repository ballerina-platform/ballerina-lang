function main (string[] args) {
    test t = create test("a",<caret>)
}

connector test (string a, string b) {

}
