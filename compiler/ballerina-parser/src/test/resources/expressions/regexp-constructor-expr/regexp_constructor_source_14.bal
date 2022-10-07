function foo() {
    x = re `[^ad]`;
    x = re `[ad]`;
    x = re `[^ad-h]`;
    x = re `[^\u{41d}-hM]`;
    x = re `[^\u{41d}-hM-N]`;
    x = re `[^\nd-hM-N\tQ-T]`;
    x = re `[^\-d-hM-N\tQ-T]`;
    x = re `[\-d-hM-N\tQ-T]`;
    x = re `[^\-\tA]`;
    x = re `[^\-\u{1234A}]`;
}
