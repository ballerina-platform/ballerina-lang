string _ = fn();
string _ = let var v = fn() in v * 2;

function name() {
    string _ = fn();
    string _ = let var v = fn() in v * 2;
}

function fn() returns int => 2;
