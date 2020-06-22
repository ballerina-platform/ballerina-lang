public function foo() {
    lock {

        int age = 10 * 2;
        string name;
        int status = 0;
        float score;
        
        type typeN object {
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
        };
    }
}
