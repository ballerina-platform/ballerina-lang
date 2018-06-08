import ballerina/io;

// Shared counter variable among multiple workers.
int counter;

function main(string... args) {
    process();
    io:println("final counter value - ", counter);
}
function process() {
    worker w1 {
        // Lock the shared variable and increment the counter.
        foreach i in 1 ... 1000 {
            lock {
                // Lock the shared variable and increment the counter.
                counter = counter + 1;
            }
        }
    }
    worker w2 {
        foreach i in 1 ... 1000 {
            lock {
                // Lock the shared variable and increment the counter.
                counter = counter + 1;
            }
        }
    }
    worker w3 {
        foreach i in 1 ... 1000 {
            lock {
                // Lock the shared variable and increment the counter.
                counter = counter + 1;
            }
        }
    }
    worker w4 {
        foreach i in 1 ... 1000 {
            lock {
                // Lock the shared variable and increment the counter.
                counter = counter + 1;
            }
        }
    }
}
