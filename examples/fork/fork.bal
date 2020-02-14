import ballerina/io;

public function main() {

    // The `fork` block allows you to spawn (fork) multiple workers
    // within any execution flow of a Ballerina program.
    fork {
        worker w1 returns [int, string] {
            int i = 23;
            string s = "Colombo";
            io:println("[w1] i: ", i, " s: ", s);
            // Return of worker `w1`.
            return [i, s];
        }

        worker w2 returns float {
            float f = 10.344;
            io:println("[w2] f: ", f);
            // Return of worker `w2`.
            return f;
        }
    }

    // Workers are visible outside the `fork` as futures.
    // The `wait` action will wait for both workers `w1` and `w2` to finish.
    record {[int, string] w1; float w2;} results = wait {w1, w2};

    // The resulting record contains returned values from each worker with
    // the field name as the worker name (if a field name is not provided).
    var [iW1, sW1] = results.w1;
    var fW2 = results.w2;
    io:println("[main] iW1: ", iW1, " sW1: ", sW1, " fW2: ", fW2);
}
