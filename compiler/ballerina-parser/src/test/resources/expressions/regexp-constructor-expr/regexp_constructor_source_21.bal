function foo() {
    x = re `\p{$}`;
    x = re `\P{$}`;
    x = re `\p{sc=$}`;
    x = re `\P{gc=$}`;
    x = re `\p{gc=a#}`;
    x = re `\P{sc=a#}`;
}
