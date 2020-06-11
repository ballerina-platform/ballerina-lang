int[] [] = v;
int[k] [a] = v;
int[*] [a, b] = v;
int[] [a, ...b] = v;
int[k] [a, [x, y], _ ] = v;

function foo() {
    int[k] [];
    int[] [a];
    int[] [a, b];
    int[k] [a, ...b];
    int[*] [a, [x, y], _ ];
}
