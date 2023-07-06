import ballerina/io;

public function main() returns error? {
    // The retry statement provides a general-purpose retry
    // facility, which is independent of the transactions.
    // Here, retrying happens according to the default retry manager
    // since there is no custom retry manager being passed to 
    // the retry operation.
    // As defined, retrying happens for maximum 3 times.
    retry (3) {
        io:println("Attempting execution...");
        // Calls a function, which simulates an error scenario to 
        // trigger the retry operation.
        return doWork();
    }
}

int count = 0;

// The function, which may return an error.
function doWork() returns error? {
    if count < 1 {
        count += 1;
        return error("Execution Error");
    } else {
        io:println("Work done.");
    }
}
