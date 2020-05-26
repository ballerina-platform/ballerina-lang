T[] [] = v;
T[k] [a] = v;
T[*] [a, b] = v;
T[] [a, ...b] = v;
T[k] [a, [x, y], _ ] = v;

function foo() {
    T[k] [];
    T[] [a];
    T[] [a, b];
    T[k] [a, ...b];
    T[*] [a, [x, y], _ ];
}
