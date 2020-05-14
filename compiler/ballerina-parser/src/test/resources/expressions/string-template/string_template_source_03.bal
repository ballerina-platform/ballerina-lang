function foo() {
    string s1 = string `${expr1 expr2}`;
    string s2 = string `${ string `hello ${ string `${expr1 + }` } world` }`;
}
