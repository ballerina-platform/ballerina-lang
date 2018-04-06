
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