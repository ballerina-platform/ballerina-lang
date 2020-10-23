T { } = x;
T { a, b:c, ...d } = x;
T { ...d } = x;
T { a, b:{ p, q:r, ...s }, ...d } = x;
T { a, b:[p, _, ...s], ...d } = x;

function foo() {
    T { } = x;
    T { a, b:c, ...d } = x;
    T { ...d } = x;
    T { a, b:{ p, q:r, ...s }, ...d } = x;
    T { a, b:[p, _, ...s], ...d } = x;
}
