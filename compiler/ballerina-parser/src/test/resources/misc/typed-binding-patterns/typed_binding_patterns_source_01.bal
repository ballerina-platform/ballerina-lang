int [] = v;
int [a] = v;
int [a, b] = v;
var [a, ...b] = v;
var [a, [x, y], _] = v;

function foo() {
    int [];
    int [a] = v;
    int [a, b];
    int [a, ...b];
    int [a, [x, y], _];
}
