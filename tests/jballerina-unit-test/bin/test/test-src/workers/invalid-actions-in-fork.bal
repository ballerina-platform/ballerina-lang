function testInvalidActionsFromFork() {
    fork {
        worker w1 {
            int i =10;
            i -> w3;
            fork {
                worker w3 {
                    int _ = <- w1;
                }
            }
        }
    }
}
