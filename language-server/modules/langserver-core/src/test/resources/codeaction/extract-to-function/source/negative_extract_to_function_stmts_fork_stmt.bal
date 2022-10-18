function testFunction() {
    int a = 5;

    fork {
        worker w1 {
            int x = 10;
            x -> w2;
        }

        worker w2 {
            int y = <- w1;
            int z = y;
        }
    }
}
