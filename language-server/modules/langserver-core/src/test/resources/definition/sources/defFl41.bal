public type SimpleTypeDesc int;
public type SimpleTypeDesc2 float;

public type UnionTypeDesc SimpleTypeDesc|SimpleTypeDesc2;

public type OptionalTypeDesc SimpleTypeDesc?;

function testTypeDescFunction() {
    UnionTypeDesc tDesc1;
    SimpleTypeDesc|SimpleTypeDesc2 tDesc2 = 1;

    OptionalTypeDesc tDesc3;
    SimpleTypeDesc? tDesc4;
}
