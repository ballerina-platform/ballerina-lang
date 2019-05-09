import testorg/foo version v1;

public function testObjectWithInterface () returns (int, string) {
    foo:Country p = new foo:Country();
    return (p.attachInterface(7), p.month);
}
