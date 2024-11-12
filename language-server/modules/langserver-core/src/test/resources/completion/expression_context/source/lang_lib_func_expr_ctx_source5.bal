function generateIterable(int a, int b) returns int[] {
    return [a, b];
}

function processArray() {
    _ = generateIterable(1, 2).'map(f);
}
