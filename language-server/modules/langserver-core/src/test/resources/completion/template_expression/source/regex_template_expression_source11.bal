function testReAtom() {
    // re `(<cursor>)`
    string:RegExp r1 = re `()`;
    
    // re `(?<cursor>)`
    string:RegExp r2 = re `(?)`;
    
    // re `(?i<cursor>)`
    string:RegExp r3 = re `(?i)`;
    
    // re `(?i-<cursor>)`
    string:RegExp r4 = re `(?i-)`;
    
    // re `(?i-m<cursor>)`
    string:RegExp r5 = re `(?i-m)`;
}
