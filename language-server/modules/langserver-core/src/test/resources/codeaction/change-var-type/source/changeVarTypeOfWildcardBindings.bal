string _ = fn();
string _ = let var v = fn() in v * 2;

function name() {
    string _ = fn();
    string _ = let var v = fn() in v * 2;
}

function fn() returns int => 2;

function query() {
    int _ = from string item in ["aa", "bb", "cc"] select item;

    string _ = from var i in [1, 2, 3, 4] collect sum(i);
}
