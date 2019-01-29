import org.foo.baz;

function textPrivateRecordAccess1() {
    baz:ParentFoo ps = {i:12, c:{name:"Mad"}};
}

function textPrivateRecordAccess2() {
    var p = baz:newPrivatePerson();
}

function textPrivateRecordAccess3() {
   string name = baz:privatePersonAsParam({age:21, name:"Mad"});
}

function textPrivateRecordAccess4() {
    var p = baz:privatePersonAsParamAndReturn({age:21, name:"Mad"});
}

function textPrivateRecordAccess5() {
    baz:PrivatePerson p = {age:21, name:"Mad"};
}
