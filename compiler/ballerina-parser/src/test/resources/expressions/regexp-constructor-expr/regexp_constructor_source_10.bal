function foo() {
    x = re `[^\p{sc=Latin}]`;
    x = re `[^\P{sc=Latin}]`;
    x = re `[\p{sc=Latin}]`;
    x = re `[^\p{gc=Lu}]`;
    x = re `[^\P{gc=Lu}]`;
    x = re `[\p{gc=Lu}]`;
    x = re `[^\p{Lu}]`;
    x = re `[^\P{Lu}]`;
    x = re `[\P{Lu}]`;
}
