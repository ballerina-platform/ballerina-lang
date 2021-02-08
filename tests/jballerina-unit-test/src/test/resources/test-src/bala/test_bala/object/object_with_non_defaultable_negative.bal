import testorg/foo;

foo:Architect pp = new(1, "John");

public function testObjectWithInterface () returns [int, string] {
    foo:Architect p;
    return [p.pp, p.name];
}
