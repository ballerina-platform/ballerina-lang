function testWorkerInsideLock() {
    lock {
        fork {
            worker w1 {
                int i = 5;
            }
        }
    }
}
