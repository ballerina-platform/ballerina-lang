function testReQuantifier() {
    // re `a<cursor>`
    string:RegExp r1 = re `a`;
    
    // re `abc<cursor>`
    string:RegExp r2 = re `abc`;
    
    // re `abc<cursor>def`
    string:RegExp r3 = re `abcdef`;
    
    // re `[a-z]<cursor>`
    string:RegExp r4 = re `[a-z]`;
    
    // re `[a-z]abc<cursor>def`
    string:RegExp r5 = re `[a-z]abcdef`;
    
    // re `(abc<cursor>def)`
    string:RegExp r6 = re `(abcdef)`;
    
    // re `(ab{5}cd<cursor>ef)`
    string:RegExp r7 = re `(ab{5}cdef)`;
    
    // re `(abc)<cursor>`
    string:RegExp r8 = re `(abc)`;
    
    // re `\w<cursor>`
    string:RegExp r9 = re `\w`;
    
    // re `abc{12<cursor>}`
    string:RegExp r10 = re `abc{12}`;
    
    // re `abc*<cursor>`
    string:RegExp r11 = re `abc*`;
    
    // re `abc+<cursor>`
    string:RegExp r12 = re `abc+`;
        
    // re `abc?<cursor>`
    string:RegExp r13 = re `abc?`;
         
    // re `abc{12}<cursor>`
    string:RegExp r14 = re `abc{12}`;
    
    // re `[<cursor>]`
    string:RegExp r15 = re `[]`;
}
