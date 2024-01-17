function foo() {
    x = re `(AAD*)`;
    x = re `([^\-d-hM-N\tQ-T])`;
    x = re `(a{1,3})`;
    x = re `(A.*?)`;
    x = re `([^ABC{1,3}])`;
    x = re `([\u{1234}])`;
    x = re `([\t]*)`;
    x = re `([\-d-hM-N\tQ-T])`;
    x = re `([\-])`;
    x = re `([\\-])`;
    x = re `(x|y|z*)`;
}
