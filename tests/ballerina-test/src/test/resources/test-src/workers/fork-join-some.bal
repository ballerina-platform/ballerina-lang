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
                any[] abc;
                abc =check <any[]> airlineResponses["ABC_Airline"];
                results[0] = <string> abc[0];
            }
            if (airlineResponses["XYZ_Airline"] != null) {
                any[] xyz;
                xyz =check <any[]> airlineResponses["XYZ_Airline"];
                results[0] = <string> xyz[0];
            }
            return results;
        } timeout (30000) (map airlineResponses) {
            results[0] = "";
            return results;
        }
}