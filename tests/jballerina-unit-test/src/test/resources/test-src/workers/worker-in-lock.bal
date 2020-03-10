function testWorkerInsideLock() {
    lock {
        fork {
            worker w1 {
                int i = 5;
            }
        }
    }
}

function testStartInsideLock() {
    lock {
        _ = start testFunction();
    }
}

function testFunction() {
    int i = 1;
}


function testWorkerInsideLockDepth3() {
    lock {
        fork {
            worker w1 {
                fork {
                    worker w2 {
                        int i = 5;
                    }
                }
            }
        }
    }
}

function testWorkerInsideNestedLocks() {
    lock {
        lock {
            int i = 10;
        }
        fork {
            worker w1 {
                int j = 15;
            }
        }
    }
}
