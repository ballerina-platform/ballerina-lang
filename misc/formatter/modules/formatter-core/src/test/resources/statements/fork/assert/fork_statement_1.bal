public function foo() {
    fork {
        worker w1 returns int {
            return 20;
        }
    }
}
