int total = sum(1, 2);
function () returns int lazyTotal = function () returns int { return twice(total); };
[int, string] [count, label] = [1, "s"];
record {| int p; string q; |} {p, q} = {p: 2, q: "t"};

public function main() {
    int doubled = twice(total);
    _ = sum(doubled, total);
}

function sum(int a, int b) returns int {
    return a + b;
}

function twice(int value) returns int {
    return sum(value, value);
}
