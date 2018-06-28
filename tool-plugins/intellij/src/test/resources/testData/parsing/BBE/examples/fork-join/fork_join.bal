import ballerina/io;

function main(string... args) {
    // The fork-join allows developers to spawn (fork) multiple workers within a Ballerina program, join the results of
    // those workers, and execute code on joined results.
    // Fork worker w1 and w2.
    fork {
        worker w1 {
            int i = 23;
            string s = "Colombo";
            io:println("[w1] i: ", i, " s: ", s);
            // The reply to the join block from worker w1.
            (i, s) -> fork;
        }

        worker w2 {
            float f = 10.344;
            io:println("[w2] f: ", f);
            // The reply to the join block from worker w2.
            f -> fork;
        }
        // The `all` condition makes the `join` block wait until all the workers have sent in their replies.
        // Once all the workers have replied, the replies are stored in the `result` variable as a map.
    } join (all) (map results) {
        // Get the values received from worker 'w1'.
        int iW1;
        string sW1;
        (iW1, sW1) = check <(int, string)>results["w1"];
        io:println("[join-block] iW1: ", iW1, " sW1: ", sW1);
        float fW2 = check <float>results["w2"];
        io:println("[join-block] fW2: ", fW2);
    }
}
