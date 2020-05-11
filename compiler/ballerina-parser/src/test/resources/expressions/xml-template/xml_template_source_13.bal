function foo() {
    int x = xml `<foo a=></foo>`;
    int x = xml `<foo a"hello"></foo>`;
    int x = xml `<foo a= b "hello"></foo>`;
}
