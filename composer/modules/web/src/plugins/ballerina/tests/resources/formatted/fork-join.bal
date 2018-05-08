import ballerina/lang.system;

function main (string... args) {
    //fork-join allows developers to spawn(fork) multiple workers within a ballerina program and join
    //the results from those workers and execute code on joined results.
    fork {
        worker w1 {
            int i = 23;
            string s = "Colombo";
            system:println("[w1] i: " + i + " s: " + s);
            // Reply to the join block from worker w1.
            i, s -> fork;
        }

        worker w2 {
            float f = 10.344;
            system:println("[w2] f: " + f);
            // Reply to the join block from worker w2.
            f -> fork;
        }

    } join (all) (map results) {
        //Here we use "all" as the join condition which means wait for all the workers.
        //When the join condition has been satisfied, results 'map' will be filled with
        //the returned messages from the workers.

        // Get values received from worker 'w1'.
        var resW1, _ = (any[])results["w1"];
        var iW1, _ = (int)resW1[0];
        var sW1, _ = (string)resW1[1];
        system:println("[join-block] iW1: " + iW1 + " sW1: " + sW1);
        // Get values received from worker 'w2'.
        var resW2, _ = (any[])results["w2"];
        var fW2, _ = (float)resW2[0];
        system:println("[join-block] fW2: " + fW2);
    }
}
