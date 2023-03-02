function testUnicodePropertyEscape() {
    // re `\<cursor>`
    string:RegExp r1 = re `\`;

    // re `\p<cursor>`
    string:RegExp r2 = re `\p`;

    // re `\p{<cursor>}`
    string:RegExp r3 = re `\p{}`;

    // re `\p{gc=<cursor>}`
    string:RegExp r4 = re `\p{gc=}`;
    
    // re `\p{gc=<cursor>`
    string:RegExp r5 = re `\p{gc=`;

    // re `\p{gc=L<cursor>}`
    string:RegExp r6 = re `\p{gc=L}`;

    // re `\p{L<cursor>}`
    string:RegExp r7 = re `\p{L}`;
}

function testUnicodePropertyNegativeCases() {
    // re `\p{<cursor>`
    string:RegExp r1 = re `\p{`;
    
    // re `\p{sc=<cursor>}`
    string:RegExp r2 = re `\p{sc=}`;

    // re `\p{gc=<cursor>L}`
    string:RegExp r3 = re `\p{gc=L}`;

    // re `\p{gc=<cursor>l}`
    string:RegExp r4 = re `\p{gc=l}`;

    // re `\p{gx=<cursor>}`
    string:RegExp r5 = re `\p{gx=}`;

    // re `\p{K<cursor>}`
    string:RegExp r6 = re `\p{K}`;
}
