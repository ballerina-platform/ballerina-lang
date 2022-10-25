function foo() {
    x = re `^ab${b}cd*e$`;
    x = re `$[^a-b\tx-z]+(?i:ab^c[d-f]){12,}`;
    x = re `(x|y)|z|[cde-j]*|(?ms:[^])`;
    x = re `[^ab-cdk-l]*${b}+|(?:pqr{1,}${a}*)`;
    x = re `a\td-*ab[^c-f]+(?m:xj(?i:x|y))`;
}
