public function foo() {
    fork {
        worker w1 returns int {
            int i = 23;

            return i;
        }
    }
}
