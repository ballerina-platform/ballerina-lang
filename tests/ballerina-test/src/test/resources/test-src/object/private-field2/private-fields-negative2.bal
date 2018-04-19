import org.foo.baz;

function textPrivateObjAccess1() {
    baz:FooPerson fooP = baz:createObj();
    var a = fooP.family;
}

function textPrivateObjAccess2() {
    baz:FooDepartment fooD = baz:createObjOfObj();
    var a = fooD.employees[0].family;
}

function textPrivateObjAccess3() {
    baz:FooEmployee fooE = baz:createAnonObj();
    var a = fooE.address;
}
