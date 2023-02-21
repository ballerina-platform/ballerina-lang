type AnnotType record {|
   string:RegExp pattern; 
|};

annotation AnnotType Annot on function;

@Annot { 
    pattern: re ``
}
function func1() {
}

@Annot { 
    pattern: re `()`
}
function func2() {
}

@Annot { 
    pattern: re `\`
}
function func3() {
}
