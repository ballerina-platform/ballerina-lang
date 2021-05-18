
object {
    public int age;
    public string name;
} p1 = object {
           public int age = 0;
           public string name = "";
           function init() {
               self.age = 1;
               self.name = "Anne";
           }
       };

object {
    public int age;
    public string name;
    function test();
} p2 = object {
    public int age = 0;
    public string name = "";
};

function test() returns int {
    object {
        public int age;
        public string name;
    } p3 = new Foo();
    object {
        public int age;
        public string name;
        function test();
    } p4 = p3;
    p4 = new Foo();
    object {
        public int age;
        public string name;
        function test();
    } p5 = object {
        public int age = 0;
        public string name = "";
    };
    return 1;
}

class Foo {
    public int age = 0;
    public string name = "";
}
