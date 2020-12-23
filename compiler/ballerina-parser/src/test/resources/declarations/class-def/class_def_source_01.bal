class Foo {

    *A;

    int age = 10 * 2;
    string name;
    public int status = 0;
    private float score;

    public function init() {
    }

    *B;

    function getName() {
    }

    remote function get() {

    }

    object {
        *A;
        int age;
        string name;
        public int status;
        public float score;

        public function init();

        *B;

        function getName();
    } parent;
}
