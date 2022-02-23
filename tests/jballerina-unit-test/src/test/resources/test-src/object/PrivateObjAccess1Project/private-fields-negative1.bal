import test/pkg.org_foo_baz as baz;

function textPrivateObjAccess1() {
    baz:ParentFoo _ = new(12, new("Mad"));
}

function textPrivateObjAccess2() {
    var _ = baz:newPrivatePerson();
}

function textPrivateObjAccess3() {
   string _ = baz:privatePersonAsParam(new (21, "Mad"));
}

function textPrivateObjAccess4() {
    var _ = baz:privatePersonAsParamAndReturn(new (21, "Mad"));
}

function textPrivateObjAccess5() {
    baz:PrivatePerson p = new (21, "Mad");
    string _ = p.getPrivatePersonName();
}
