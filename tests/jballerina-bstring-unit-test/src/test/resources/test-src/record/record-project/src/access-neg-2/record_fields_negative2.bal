import org.foo.baz;

function textPrivateRecordAccess1() {
    baz:FooPerson fooP = baz:createRecord();
    var a = fooP.family;
}

function textPrivateRecordAccess2() {
    baz:FooDepartment fooD = baz:createRecordOfRecord();
    var a = fooD.employees[0].family;
}

function textPrivateRecordAccess3() {
    baz:FooEmployee fooE = baz:createAnonRecord();
    var a = fooE.address;
}
