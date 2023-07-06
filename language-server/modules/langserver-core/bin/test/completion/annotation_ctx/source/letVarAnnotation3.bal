import ballerina/module1;

public type AnnotationType record {
    string foo;
    int bar?;
};

public const annotation AnnotationType a1 on source var;

public const annotation a2 on source var;

public function main() {
    int a = let @module1: int b = 1 in b * 2;
    int c = 123; 
}
