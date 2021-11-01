import recordproject.org.foo.baz;

function textPrivateRecordAccess1() {
    baz:ParentFoo _ = {i:12, c:{name:"Mad"}};
}

function textPrivateRecordAccess2() {
    var _ = baz:newPrivatePerson();
}

function textPrivateRecordAccess3() {
   string _ = baz:privatePersonAsParam({age:21, name:"Mad"});
}

function textPrivateRecordAccess4() {
    var _ = baz:privatePersonAsParamAndReturn({age:21, name:"Mad"});
}

function textPrivateRecordAccess5() {
    baz:PrivatePerson _ = {age:21, name:"Mad"};
}
