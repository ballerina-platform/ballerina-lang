import ballerina/io;

// The `counter` variable will be shared among multiple workers.
int counter = 0;

public function main() {
    process();
    io:println("final counter value - ", counter);
    io:println("final count field value - ", counterObj.count);
}

type Counter object {
    int count = 0;

    public function update() {
        foreach var i in 1 ... 1000 {
            lock {
                // Locks the `count` field variable and increments the `count`.
                // The `count` field of the same object instance will be locked.
                self.count = self.count + 1;
            }
        }
    }
};
// The `counterObj` object instance will be shared among multiple workers.
Counter counterObj = new;

function process() {
    worker w1 {
        counterObj.update();
        // Locks the shared `counter` variable and increments the `counter`.
        foreach var i in 1 ... 1000 {
            lock {
                // Locks the shared `counter` variable and increments the `counter`.
                counter = counter + 1;
            }
        }
    }
    worker w2 {
        counterObj.update();
        foreach var i in 1 ... 1000 {
            lock {
                // Locks the shared `counter` variable and increments the `counter`.
                counter = counter + 1;
            }
        }
    }
    worker w3 {
        counterObj.update();
        foreach var i in 1 ... 1000 {
            lock {
                // Locks the shared `counter` variable and increments the `counter`.
                counter = counter + 1;
            }
        }
    }
    worker w4 {
        counterObj.update();
        foreach var i in 1 ... 1000 {
            lock {
                // Locks the shared `counter` variable and increments the `counter`.
                counter = counter + 1;
            }
        }
    }
    // Waits for all workers to complete.
    var result = wait {w1, w2, w3, w4};
}
