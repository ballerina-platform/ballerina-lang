import ballerina/io;

function main (string[] args) {
    //The fork-join allows developers to spawn (fork) multiple workers within a ballerina program, join the results of
    those workers, and execute code on joined results.
    //In this example, you are forking worker w1 and w2.
    fork {
        worker w1 {
            int i = 23;
            string s = "Colombo";
            io:println("[w1] i: " + i + " s: " + s);
            // The reply to the join block from worker w1.
            i, s -> fork;
        }

        worker w2 {
            float f = 10.344;
            io:println("[w2] f: " + f);
            // The reply to the join block from worker w2.
            f -> fork;
        }

    } join (all) (map results) {
        // The 'all' condition makes the join blokc wait until all the workers have sent in their replies.
        //Once all the workers have replied, the replies are stored in the 'result' variable as a map.


        // Get the values received from worker 'w1'.
        any[] resW1 = check <any[]>results["w1"];
        int iW1 = check <int>resW1[0];
        string sW1 = <string>resW1[1];
        io:println("[join-block] iW1: " + iW1 + " sW1: " + sW1);
        // Get the values received from worker 'w2'
        any[] resW2 = check <any[]>results["w2"];
        float fW2 = check <float>resW2[0];
        io:println("[join-block] fW2: " + fW2);
    }
}
