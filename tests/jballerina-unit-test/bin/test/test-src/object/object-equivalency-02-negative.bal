import testorg/objectpkg.org_foo as foo;
import testorg/objectpkg.org_foo_bar as bar;

function handleUser(foo:user u) returns (string) {
    return u.name;
}


function testStructEqViewFromThirdPackage1() returns (string) {
    bar:userBar ub = new ("ball");
    return handleUser(<foo:userFoo>ub);
}


function testStructEqViewFromThirdPackage2() returns (string) {
    bar:BarObj barObj = new();
    foo:FooObj fooObj = <foo:FooObj> barObj;
    return fooObj.name;
}