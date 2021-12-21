function createService(int age) returns object {
    public function getSum(int age) returns int;
} {

    var httpService =
    object {
        public function getSum(int age) returns int {
            return 1 + age;
        }
    };

    return httpService;
}

public function main() {
    var obj = createService(200);
}
