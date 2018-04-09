function testForkJoinAny() returns string[] {

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
                results[0] = <string> airlineResponses["ABC_Airline"];
            }
            if (airlineResponses["XYZ_Airline"] != null) {
                results[0] = <string> airlineResponses["XYZ_Airline"];
            }
            return results;
        } timeout (30000) (map airlineResponses) {
            results[0] = "";
            return results;
        }
}