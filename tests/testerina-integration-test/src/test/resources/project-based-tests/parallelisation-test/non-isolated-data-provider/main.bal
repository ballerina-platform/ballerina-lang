import ballerina/io;
import ballerina/lang.runtime;
import ballerina/time;

int employedCount = 20000;
int[] quantities_ = [
    1,
    2,
    3,
    4,
    5,
    6,
    7,
    8,
    9,
    10,
    11,
    12,
    13,
    14,

    15,
    16,
    17,
    18,
    19,
    20,
    21,
    22,

    23,
    24
];

public function main() {
    decimal now = time:monotonicNow();
    int count = 10;

    while true {
        if count > 0 {
            future<()> f = start get_average();
            count -= 1;
        }
        // int avg = get_average();
        // io:println("Average: ", avg);

    }

}

function get_average() {
    runtime:sleep(10);
    io:println("Helllwo");

}
