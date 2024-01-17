function foo() {
    x = re `[a]`;
    x = re `[ab]`;
    x = re `[abc]`;
    x = re `[abc-iabc-ia]`;
    x = re `[abc-]`;
    x = re `[ab-cef]`;
    x = re `[ab-cefg-hijx-lm]`;
    x = re `[ab-c-]`;
    x = re `[ab-c-d]`;
    x = re `[ab-c--]`;
    x = re `[ab-c---]`;
    x = re `[ab-c--defg-h]`;
}
