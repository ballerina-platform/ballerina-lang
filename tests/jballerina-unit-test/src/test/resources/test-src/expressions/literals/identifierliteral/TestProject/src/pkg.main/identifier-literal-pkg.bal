
import pkg.variable;

function getVarsInOtherPkg() returns [int, string, float, any] {
    return [variable: 'getVariable\ Int(), variable: 'getVariable\ String(), variable: 'getVariable\ Float(),
    variable: 'getVariable\ Any()];
}

function accessStructWithIL()returns [string, int] {
    return [variable: 'getPerson\ 1().'first\ name, variable: 'getPerson\ 1().'current\ age];
}
