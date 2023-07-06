import test/pkg.org_foo_baz_sn as baz;

function textPrivateObjAccess1() {
    baz:FooPerson fooP = baz:createObj();
    var _ = fooP.family;
}

function textPrivateObjAccess3() {
    baz:FooEmployee fooE = baz:createAnonObj();
    var _ = fooE.address;
}
