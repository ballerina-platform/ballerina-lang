import cyclic.mod1;

public function hello(string name) returns string {
    _ = mod1:hello("A");
    if name !is "" {
        return "Hello, " + name;
    }
    return "Hello, World!";
}
