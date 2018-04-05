
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
            any[] abc;
            any[] xyz;
            abc =check <any[]> airlineResponses["ABC_Airline"];
            xyz =check <any[]> airlineResponses["XYZ_Airline"];
            results[0] =check <int> abc[0];
            results[1] =check <int> xyz[0];
        } timeout (30000) (map airlineResponses) {
            results[0] = -1;
            results[0] = -1;
        }
        return results;
}