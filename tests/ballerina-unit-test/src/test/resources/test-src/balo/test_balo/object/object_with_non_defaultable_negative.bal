import testorg/foo version v1;

foo:Architect pp = new(1, "John");

public function testObjectWithInterface () returns (int, string) {
    foo:Architect p;
    return (p.attachInterface(7), p.name);
    //return (5, "kk");
}
