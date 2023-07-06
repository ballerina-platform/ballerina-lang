function foo() {
    var x = xml `<!---->`;
    var x = xml `<!-- hello -->`;
    var x = xml `<foo><!--hello--></foo>`;
    var x = xml `<foo> bar <!-- hello --> baz</foo>`;
}
