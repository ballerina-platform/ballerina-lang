import org.foo.baz;

function textPrivateObjAccess1() {
    baz:ParentFoo ps = new(12, new("Mad"));
}

function textPrivateObjAccess2() {
    var p = baz:newPrivatePerson();
}

function textPrivateObjAccess3() {
   string name = baz:privatePersonAsParam(new (21, "Mad"));
}

function textPrivateObjAccess4() {
    var p = baz:privatePersonAsParamAndReturn(new (21, "Mad"));
}

function textPrivateObjAccess5() {
    baz:privatePerson p = new (21, "Mad");
    string name = p.getPrivatePersonName();
}
