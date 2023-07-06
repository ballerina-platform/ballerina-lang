var {a, b:{p q:r, ...s }, ...d } = x;
var [{a, b:{p, q:r, ... , ...d }, l] = x;
var {a, b % :error()} = x;

function foo() {
    var {a, b:{p, q:r, ...s }, ...d } = x;
    var [{a b:{p, q:r, ...s }, ...d }, ] = x;
}
