import cyclic.mod2;

public function hello(string name) returns string {
    _ = mod2:hello("A");
    if name !is "" {
        return "Hello, " + name;
    }
    return "Hello, World!";
}
