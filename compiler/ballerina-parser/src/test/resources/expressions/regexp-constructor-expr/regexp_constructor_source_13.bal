function foo() {
    x = re `[^a-d]`;
    x = re `[a-d]`;
    x = re `[^A-D\-]`;
    x = re `[^m-pr-u]`;
    x = re `[^\u{41}-d\u{55}-\u{58}]`;
    x = re `[^\u{41}-d\u{46}i]`;
    x = re `[^AB-A]`;
    x = re `[^ABCg-j]`;
}
