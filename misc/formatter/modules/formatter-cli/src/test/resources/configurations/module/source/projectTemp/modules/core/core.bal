public const OFFSET = 10;
public const MULTIPLIER = 2;
public const TRUE = true;
public const FALSE = false;

function bar(string|int x) {
    string|int y = x;
    if y is string {
        y += "a";
    } else {
        y += 2;
    }
}
