public function main (string... args) {
    int[] intArr = [1,2,3];
    f1(intArr, 1, 2);
}

// Logical correctness of following function is irrevant to this compiler unit tests.
function f1 (int[] arr, int l, int r) {
    if (l == r) {
        return;
    }
    f1(arr, 1, 1);
    f1(arr, 2, 2);
    f1(arr, 3, 3);
    f1(arr, 4, 4);

    string taintedValue = f2(arr, l, r);
    secureFunction(taintedValue, taintedValue);
}

function f2 (int[] arr, int l, int r) returns @tainted string {
    return "tainted-value";
}

function secureFunction(@untainted string secureIn, string insecureIn) {

}
