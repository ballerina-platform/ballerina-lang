type Foo record {|
    record {|
        int x;
    |} natural;
|};

string natural = "natural";

public function main() returns error? {
    string foo = natural + " number";

    int val = check natural(1) + 5;

    Foo f = {natural: {x: 1}};
    f.natural.x = 3;
}

function natural(int x) returns int {
    return x * 2;
}
