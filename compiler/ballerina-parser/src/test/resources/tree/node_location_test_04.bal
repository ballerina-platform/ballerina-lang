import foobar/bar;
import foobar/baz;
import foobar/qux;
import sammj/adder;

public function main(string[] args) returns error? {
    int a = args[0];
    int b = args[1];
    check adder:add(a, b);
}
