import ballerina/module1;

function getTDesc() returns typedesc<function()> {
    typedesc<function()> td = typeof testFunction;

    td.@module1:
    
    return td;
}

function testFunction() {
    int value = 12;
}

type AnnotationType record {
    string foo;  
    int bar?;
};

annotation AnnotationType a1 on function;
