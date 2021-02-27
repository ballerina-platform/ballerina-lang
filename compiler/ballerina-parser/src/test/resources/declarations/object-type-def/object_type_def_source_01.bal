type Foo object {

    *A;

    int age;
    string name;
    public int status;
    public float score;

    public function init();

    *B;

    function getName();

    remote function get();

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
};
