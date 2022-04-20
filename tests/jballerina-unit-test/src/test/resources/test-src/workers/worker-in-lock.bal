function testWorkerInsideLock() {
    lock {
        fork {
            worker w1 {
                int _ = 5;
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
    int _ = 1;
}


function testWorkerInsideLockDepth3() {
    lock {
        fork {
            worker w1 {
                fork {
                    worker w2 {
                        int _ = 5;
                    }
                }
            }
        }
    }
}

function testWorkerInsideNestedLocks() {
    lock {
        lock {
            int _ = 10;
        }
        fork {
            worker w1 {
                int _ = 15;
            }
        }
    }
}
