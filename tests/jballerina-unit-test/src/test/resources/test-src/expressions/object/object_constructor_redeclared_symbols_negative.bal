function createService(int age) returns object {
    public function getSum(int age) returns int;
} {

    var httpService =
    object {
        public function getSum(int age) returns int {
            return 1 + age;
        }

        public function getAge(int age3, int age) returns int {
            int age = 4 + age3;
            return 0;
        }

        public function getAgeOf() returns int {
            int age = 4;
            return 0;
        }

    };

    return httpService;
}
