import org.foo.baz;

function textPrivateStructAccess1() {
    baz:ParentFoo ps = {i:12, c:{name:"Mad"}};
}

function textPrivateStructAccess2() {
    var p = baz:newPrivatePerson();
}

function textPrivateStructAccess3() {
   string name = baz:privatePersonAsParam({age:21, name:"Mad"});
}

function textPrivateStructAccess4() {
    var p = baz:privatePersonAsParamAndReturn({age:21, name:"Mad"});
}

function textPrivateStructAccess5() {
    baz:privatePerson p = {age:21, name:"Mad"};
    string name = p.getPrivatePersonName();
}
