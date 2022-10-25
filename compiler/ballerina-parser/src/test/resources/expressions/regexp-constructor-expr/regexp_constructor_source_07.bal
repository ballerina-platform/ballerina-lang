function foo() {
    x = re `[^\u{1234}]`;
    x = re `[^\u{1F600}]`;
    x = re `[^\u{5C}]`;
    x = re `[\u{5C}]`;
    x = re `[\u{1234}]`;
}
