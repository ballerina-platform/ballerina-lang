
function testForkJoinAll()(int[]) {

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
            any[] abc;
            any[] xyz;
            abc,_ = (any[]) airlineResponses["ABC_Airline"];
            xyz,_ = (any[]) airlineResponses["XYZ_Airline"];
            results[0], _ = (int) abc[0];
            results[1], _ = (int) xyz[0];
        } timeout (30000) (map airlineResponses) {
            results[0] = -1;
            results[0] = -1;
        }
        return results;
}