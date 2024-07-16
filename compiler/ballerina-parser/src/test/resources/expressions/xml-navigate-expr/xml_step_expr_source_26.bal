function foo() {
    xml x = xml `<item><book>Study</book></item>`;
    _ = x/<book>[0];
    _ = x/<book>[1][0];

    _ = x/<book>.<name>;
    _ = x/<book>.<ns:name>;
    _ = x/<book>.<ns:name>.<sub>;
    _ = x/<book>.<ns:name|test>.<ns:game|*>;
    _ = x/<book>.<ns:name|*>;
    _ = x/<book>.<*>;

    _ = x/<book>.length();
    _ = x/<book>.data().length();
    _ = x/<book>.foo(b, 5, s = "s", ...names).bar();

    _ = x/<book>[1][0].data().<name>.foo();
    _ = x/<book>.<name>.foo().bar()[0][1];
    _ = x/<book>.data().<name>[0].<sub>[1];

    _ = x/*[0];
    _ = x/*[1][0];

    _ = x/*.<name>;
    _ = x/*.<ns:name>;
    _ = x/*.<ns:name>.<sub>;
    _ = x/*.<ns:name|test>.<ns:game|*>;
    _ = x/*.<ns:name|*>;
    _ = x/*.<*>;

    _ = x/*.length();
    _ = x/*.data().length();
    _ = x/*.foo(b, 5, s = "s", ...names).bar();

    _ = x/*[1][0].data().<name>.foo();
    _ = x/*.<name>.foo().bar()[0][1];
    _ = x/*.data().<name>[0].<sub>[1];

    _ = x/**/<book>[0];
    _ = x/**/<book>[1][0];

    _ = x/**/<book>.<name>;
    _ = x/**/<book>.<ns:name>;
    _ = x/**/<book>.<ns:name>.<sub>;
    _ = x/**/<book>.<ns:name|test>.<ns:game|*>;
    _ = x/**/<book>.<ns:name|*>;
    _ = x/**/<book>.<*>;

    _ = x/**/<book>.length();
    _ = x/**/<book>.data().length();
    _ = x/**/<book>.foo(b, 5, s = "s", ...names).bar();

    _ = x/**/<book>[1][0].data().<name>.foo();
    _ = x/**/<book>.<name>.foo().bar()[0][1];
    _ = x/**/<book>.data().<name>[0].<sub>[1];
}
