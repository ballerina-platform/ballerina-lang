type Foo object {
    public int age = 0;
    public string name = "";

    function __init() {}

    function __init() {}
};

function testInitializerInvocation() {
    Foo f = new();
    _ = f.__init();
}

type Bar object {
   private function __init() {}
};
