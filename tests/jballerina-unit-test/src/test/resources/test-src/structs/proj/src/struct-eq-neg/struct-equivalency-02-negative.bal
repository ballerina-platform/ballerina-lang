import org.foo;
import org.foo.bar;

function handleUser(foo:user u) returns (string) {
    return u.name;
}


function testStructEqViewFromThirdPackage() returns (string) {
    bar:userBar ub = {name:"ball"};
    return handleUser(checkpanic foo:userFoo.constructFrom(ub));
}