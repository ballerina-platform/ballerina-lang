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
    
    // re `(?im<cursor>)`
    string:RegExp r6 = re `(?im)`;
    
    // re `(?im-s<cursor>)`
    string:RegExp r7 = re `(?im-s)`;
    
    // re `(?-)`
    string:RegExp r8 = re `(?-)`;
    
    // re `(?-i)`
    string:RegExp r9 = re `(?-i)`;
    
    // re `(?-is)`
    string:RegExp r10 = re `(?-is)`;
}
