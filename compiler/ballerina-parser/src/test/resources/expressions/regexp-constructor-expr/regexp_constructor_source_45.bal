function foo() {
    string:RegExp x1 = re `[AB\p{gc=Lu}]+` ? `aa` ? `;
}
