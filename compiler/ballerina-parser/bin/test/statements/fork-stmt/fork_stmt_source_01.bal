public function foo() {
    fork {
        worker w1 returns int {
            int i = 23;

            return i;
        }

        worker w2 returns int {
            int f = 10;
            f += 5;

            return f;
        }
    }
}
