import ballerina/lang.system;

function main (string... args) {

    // Declare the fork-join statement
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
    } join (some 1) (map results) {
        //Here we use "some 1" as the join condition which means wait for any one of the workers.
        //When the join condition has been satisfied, results 'map' will be filled with
        //the returned messages from the workers.

        // Check whether the completed worker is 'w1'.
        if (results["w1"] != null) {
            var resW1, _ = (any[])results["w1"];
            var iW1, _ = (int)resW1[0];
            var sW1, _ = (string)resW1[1];
            system:println("[join-block] iW1: " + iW1 + " sW1: " + sW1);
        }

        // Check whether the completed worker is 'w2'.
        if (results["w2"] != null) {
            var resW2, _ = (any[])results["w2"];
            var fW2, _ = (float)resW2[0];
            system:println("[join-block] fW2: " + fW2);
        }
    }
}
