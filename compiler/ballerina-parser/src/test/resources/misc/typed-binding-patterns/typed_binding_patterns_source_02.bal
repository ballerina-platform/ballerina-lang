T [] = v;
T [a] = v;
T [a, b] = v;
T [a, ...b] = v;
T [a, [x, y], _] = v;

function foo() {
    T [];
    T [a];
    T [a, b];
    T [a, ...b];
    T [a, [x, y], _];
}
