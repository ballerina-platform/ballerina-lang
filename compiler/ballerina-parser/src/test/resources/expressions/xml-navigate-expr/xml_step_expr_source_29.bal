function foo() {
    xml x = xml `<item><book>Study</book></item>`;

    _ = x/*[];
    _ = x/*[y;
    _ = x/*[0;
    _ = x/*[0]1];
    _ = x/*[0][2;

    _ = x/*.<name;
    _ = x/*.<>;
    _ = x/*.<**>;
    _ = x/*.<>
    _ = x/*.<ns:name;

    _ = x/*.data(;
    _ = x/*.();
    _ = x/*.(;

    _ = x/*[0.data(;
    _ = x/*[0][1.data(;
    _ = x/*.()[1];

    _ = x/*[y]];
    // _ = x/*.<ns::name>; uncomment after #42726 is fixed
    _ = x/*.<book>.data());
    _ = x/*.<book>.data().<name>[0]];
    _ = x/*.<book>.data().<**>;
}
