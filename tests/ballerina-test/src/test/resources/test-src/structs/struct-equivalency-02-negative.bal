import org.foo;
import org.foo.bar;

function handleUser(foo:user u) (string) {
    return u.name;
}


function testStructEqViewFromThirdPackage() (string) {
    bar:userBar ub = {name:"ball"};
    return handleUser((foo:userFoo)ub);
}