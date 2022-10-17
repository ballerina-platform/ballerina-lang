function foo() {
    x = re `^\p{Mn}+$`;
    x = re `^\P{Mc}+$`;
    x = re `^\p{Sm}+$`;
    x = re `^\P{Zs}+$`;
    x = re `^\P{gc=Lu}+$`;
    x = re `^\P{gc=Sc}+$`;
    x = re `\P{Sc}+$`;
    x = re `^\P{gc=Sk}+$`;
    x = re `^\P{gc=L}+$`;
    x = re `^\P{Lt}+$`;
    x = re `^\P{L}+$`;
}
