var [{a, b:{p, q:r, ...s }, ...d }, l] = x;
var [{a, b:[p, _, ...s ], ...d }, l] = x;

function foo() {
    var [{a, b:{p, q:r, ...s }, ...d }, l] = x;
    var [{a, b:[p, _, ...s ], ...d }, l] = x;
}
