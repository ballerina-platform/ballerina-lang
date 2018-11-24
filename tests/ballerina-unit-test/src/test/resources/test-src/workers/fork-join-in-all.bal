function testForkJoinAll() returns int[]|error {

    int[] results = [];
    int x = 100;

    fork {
        worker ABC_Airline {
            x = 234;
            x -> fork;
        }

        worker XYZ_Airline {
            x = 500;
            x -> fork;
        }
    } join (all) (map airlineResponses) {
        int abc = check <int>airlineResponses.ABC_Airline;
        int xyz = check <int>airlineResponses.XYZ_Airline;
        results[0] = abc;
        results[1] = xyz;
    } timeout (30000) (map airlineResponses) {
        results[0] = -1;
        results[0] = -1;
    }
    return results;
}

function testForkJoinWithEmptyTimeoutBlock() returns int[] {
    forkJoinWithEmptyTimeoutBlock();
    int[] p = [];
    p[0] = 234;
    p[1] = 500;
    return p;
}
function forkJoinWithEmptyTimeoutBlock() {
    fork {
        worker worker1 {
            7 -> fork;
        }
        worker worker2 {
            6 -> fork;
        }
    } join (all)(map results) {

    } timeout (1000)(map results1) {

    }
}