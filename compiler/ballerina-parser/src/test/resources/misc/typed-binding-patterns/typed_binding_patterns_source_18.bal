var {a, b:{p q:r, ...s }, ...d } = x;
var [{a, b:{p, q:r, ... , ...d }, l] = x;

function foo() {
    var {a, b:{p, q:r, ...s }, ...d } = x;
    var [{a b:{p, q:r, ...s }, ...d }, ] = x;
}
