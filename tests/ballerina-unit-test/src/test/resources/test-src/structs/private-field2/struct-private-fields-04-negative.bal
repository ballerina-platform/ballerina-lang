import org.foo.baz;

function textPrivateStructAccess1() {
    baz:FooPerson fooP = baz:createStruct();
    var a = fooP.family;
}

function textPrivateStructAccess2() {
    baz:FooDepartment fooD = baz:createStructOfStruct();
    var a = fooD.employees[0].family;
}

function textPrivateStructAccess3() {
    baz:FooEmployee fooE = baz:createAnonStruct();
    var a = fooE.address;
}
