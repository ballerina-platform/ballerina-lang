function foo() {
    x = re `[^\p{sc=Latin}]`;
    x = re `[^\P{sc=Latin}]`;
    x = re `[\p{sc=Latin}]`;
    x = re `[^\p{gc=Letter}]`;
    x = re `[^\P{gc=Letter}]`;
    x = re `[\p{gc=Letter}]`;
    x = re `[^\p{Letter}]`;
    x = re `[^\P{Letter}]`;
    x = re `[\P{Letter}]`;
}
