import test/pkg.org_foo_baz as baz;

function textPrivateObjAccess1() {
    baz:FooPerson fooP = baz:createObj();
    var a = fooP.family;
}

function textPrivateObjAccess3() {
    baz:FooEmployee fooE = baz:createAnonObj();
    var a = fooE.address;
}
