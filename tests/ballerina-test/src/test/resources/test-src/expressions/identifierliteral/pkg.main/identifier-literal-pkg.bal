
import pkg.variable;

function getVarsInOtherPkg() returns (int, string, float, any) {
    return (variable:^"Variable Int", variable:^"Variable String", variable:^"Variable Float", variable:^"Variable Any");
}

function accessStructWithIL()returns (string, int) {
    return (variable:^"person 1".^"first name", variable:^"person 1".^"current age");
}