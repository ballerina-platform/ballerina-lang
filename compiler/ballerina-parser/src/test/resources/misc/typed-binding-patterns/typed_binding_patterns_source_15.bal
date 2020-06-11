var {a, b:{p, q:r, ...s }, ...d } = x;
var {a, b:[p, _, ...s ], ...d } = x;

function foo() {
    var {a, b:{p, q:r, ...s }, ...d } = x;
    var {a, b:[p, _, ...s ], ...d } = x;
}
