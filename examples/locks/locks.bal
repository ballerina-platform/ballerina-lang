import ballerina/io;

// Shared counter variable among multiple workers.
int counter = 0;

public function main() {
    process();
    io:println("final counter value - ", counter);
    io:println("final count field value - ", counterObj.count);
}

type Counter object {
    //shared field among same object instances
    int count = 0;

    public function update() {
        foreach var i in 1 ... 1000 {
            lock {
                // Lock the field variable and increment the count.
                // The `count' field of same object instance is locked.
                self.count = self.count + 1;
            }
        }
    }
};
// Shared object instance among multiple workers.
Counter counterObj = new;

function process() {
    worker w1 {
        counterObj.update();
        // Lock the shared variable and increment the counter.
        foreach var i in 1 ... 1000 {
            lock {
                // Lock the shared variable and increment the counter.
                counter = counter + 1;
            }
        }
    }
    worker w2 {
        counterObj.update();
        foreach var i in 1 ... 1000 {
            lock {
                // Lock the shared variable and increment the counter.
                counter = counter + 1;
            }
        }
    }
    worker w3 {
        counterObj.update();
        foreach var i in 1 ... 1000 {
            lock {
                // Lock the shared variable and increment the counter.
                counter = counter + 1;
            }
        }
    }
    worker w4 {
        counterObj.update();
        foreach var i in 1 ... 1000 {
            lock {
                // Lock the shared variable and increment the counter.
                counter = counter + 1;
            }
        }
    }
    // Wait for all workers to complete
    var result = wait {w1,w2,w3,w4};
}
