function foo() {
    x = re `^\p{ASCII}+$`;
    x = re `^\P{ASCII}+$`;
    x = re `^\p{Any}+$`;
    x = re `^\P{Assigned}+$`;
    x = re `^\P{gc=Close_Punctuation}+$`;
    x = re `^\P{gc=Sc}+$`;
    x = re `\P{Sc}+$`;
    x = re `^\P{gc=Letter}+$`;
    x = re `^\P{gc=L}+$`;
    x = re `^\P{Letter}+$`;
    x = re `^\P{L}+$`;
}
