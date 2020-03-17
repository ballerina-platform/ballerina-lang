import ballerina/io;

// This function creates a new strand that belongs to the same
// thread as the current strand.
public function case1() {
    // Execution of the `start` action causes the creation of a new strand here, and
    // it will be part of the thread executing the current strand.
    // But the Ballerina runtime will not execute the new strand until the current strand yields.
    io:println("--- case 1 ---");
    future<int> f1 = start multiply(1, 2);

    // Here, the `wait` action causes the current strand to yield.
    // Once it yields, the Ballerina runtime executes the new strand.
    io:println("Before the wait action");
    int result = wait f1;
    io:println("After the wait action\n");
}

// This function creates a new strand and it should be in a separate thread from the current strand.
// The usage of the `@strand` annotation with the `thread` field value "any" enforces this behavior.
public function case2() {
    // This new strand does not belong to the thread executing the current strand.
    // Ballerina runtime assigns this new strand to a separate thread in the runtime thread pool.
    io:println("--- case 2 ---");
    future<int> f1 = @strand {thread: "any"} start multiply(1, 2);

    io:println("Before the wait action");
    int result = wait f1;
    io:println("After the wait action\n");
}

// Create two new strands and assign them to separate threads from the thread executing the current strand.
public function case3() {
    io:println("--- case 3 ---");
    // Creates a new strand
    future<int> f1 = @strand {thread: "any"} start multiply(1, 2);
    future<int> f2 = @strand {thread: "any"} start multiply(4, 5);

    io:println("Before the wait action");
    map<int> results = wait {f1, f2};
    io:println("After the wait action\n");
}

// Create two new strands. Ballerina runtime assigns the first one to separate thread and assigns
// the second one to the same thread executing the current strand.
public function case4() {
    io:println("--- case 4 ---");
    // Creates a new strand
    future<int> f1 = @strand {thread: "any"} start multiply(1, 2);
    future<int> f2 = start multiply(4, 5);

    io:println("Before the wait action");
    map<int> results = wait {f1, f2};
    io:println("After the wait action\n");
}

// Create two new strands. Ballerina runtime assigns both strands to the same thread executing the current strand.
public function case5() {
    io:println("--- case 5 ---");
    // Creates a new strand
    future<int> f1 = start multiply(1, 2);
    future<int> f2 = start multiply(4, 5);

    io:println("Before the wait action");
    map<int> results = wait {f1, f2};
    io:println("After the wait action\n");
}

public function main() {
    case1();
    case2();
    case3();
    case4();
    case5();
}

function multiply(int x, int y) returns int {
    io:println(string `Multiplying ${x} * ${y}`);
    return x * y;
}
