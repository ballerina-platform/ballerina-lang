T [ = v;
T [a b] = v;
T [a, ...] = v;
T [a, x, y], _ ] = v;

function foo() {
    T [;
    T [a b];
    T [a, ...];
    T [a, [x, y, _ ];
}
