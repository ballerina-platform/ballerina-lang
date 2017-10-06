
function testForkJoinAny()(string[]) {

        string[] results = [];
        int i = 0;
        fork {
            worker ABC_Airline {
                "abc" -> fork;
            }
            worker XYZ_Airline {
                "xyz" -> fork;
            }
        } join (some 1) (map airlineResponses) {
            if (airlineResponses["ABC_Airline"] != null) {
                any[] abc;
                abc,_ = (any[]) airlineResponses["ABC_Airline"];
                results[0], _ = (string) abc[0];
            }
            if (airlineResponses["XYZ_Airline"] != null) {
                any[] xyz;
                xyz,_ = (any[]) airlineResponses["XYZ_Airline"];
                results[0], _ = (string) xyz[0];
            }
            return results;
        } timeout (30000) (any[][] airlineResponses) {
            results[0] = -1;
            return results;
        }
}