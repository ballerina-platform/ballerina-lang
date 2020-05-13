function foo() {
    int x = xml `<foo>${ a + }</foo>`;
    int x = xml `<foo>${ a + xml `<bar>${ b + }</bar>` }</foo>`;
}
