function foo() {
    string s1 = string `${expr1}`;
    string s2 = string `hello ${expr1} world`;
    string s3 = string `${expr1}${expr2}`;
    string s4 = string `${string `hello ${string `${expr1}${expr2}`} world`}`;
}
