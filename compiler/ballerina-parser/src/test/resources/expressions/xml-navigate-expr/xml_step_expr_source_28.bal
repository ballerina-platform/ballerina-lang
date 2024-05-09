function foo() {
    xml x = xml `<item><book>Study</book></item>`;

    _ = x/**/<book>[];
    _ = x/**/<book>[0;
    _ = x/**/<book>[0]1];
    _ = x/**/<book>[0][2;

    _ = x/**/<book>.<name;
    _ = x/**/<book>.<*;
    _ = x/**/<book>.<>
    _ = x/**/<book>.<ns:name;

    _ = x/**/<book>.data(;
    _ = x/**/<book>.();
    _ = x/**/<book>.(;

    _ = x/**/<book>[0.data(;
    _ = x/**/<book>[0][1.data(;
    _ = x/**/<book>.()[1];

    _ = x/**/<book>[y]];
    _ = x/**/<book>.<ns::name>;
    _ = x/**/<book>.data());
    _ = x/**/<book>.data().<name>[0]];
    _ = x/**/<book>.data().<**>;
}
