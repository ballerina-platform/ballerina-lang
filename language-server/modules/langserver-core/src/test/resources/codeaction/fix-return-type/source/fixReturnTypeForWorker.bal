function testFunction() {
    worker w1 {
        return "test";
    }

    worker w2 returns int {
        return "test";
    }

    fork {
        worker w3 {
            fork {
                worker w4 returns int {
                    return "test";
                }
            }

            return "test";
        }
    }
}
