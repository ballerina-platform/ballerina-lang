// x = R
// y = S

type N int;
type S string;
type R N|S;

function foo() {
    R x = 1;
    S y = "str";
}
