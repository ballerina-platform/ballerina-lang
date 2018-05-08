
function testForkJoinAll() returns int[] {

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
            int abc = check <int> airlineResponses["ABC_Airline"];
            int xyz = check <int> airlineResponses["XYZ_Airline"];
            results[0] = abc;
            results[1] = xyz;
        } timeout (30000) (map airlineResponses) {
            results[0] = -1;
            results[0] = -1;
        }
        return results;
}

function testForkJoinAllAddTest() returns int[] {
    testForkJoinAll1();
    int[] p;
    p[0] = 234;
    p[1] = 500;
    return p;
}
function testForkJoinAll1() {
    fork {
        worker worker1 {
            7 -> fork;
        }
        worker worker2 {
            6 -> fork;
        }
    }join (all)(map results) {
        //int abc = check <int> results["worker1"];
        //int[] results = [];
        //results[0] = abc;
    }timeout(100)(map results1) {
    }
}