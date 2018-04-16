function testForkJoinAnyOfSpecific () returns string[] {

    string[] results = [];
    fork {
        worker ABC_Airline {
            string m1 = "abc";
            m1 -> fork;
        }
        worker XYZ_Airline {
            string m1 = "xyz";
            m1 -> fork;
        }
        worker PQR_Airline {
            string m1 = "pqr";
            m1 -> fork;
        }
    } join (some 1 ABC_Airline, XYZ_Airline) (map airlineResponses) {
        if (airlineResponses.hasKey("ABC_Airline")) {
            results[0] = <string>airlineResponses["ABC_Airline"];
        }
        if (airlineResponses.hasKey("XYZ_Airline")) {
            results[0] = <string> airlineResponses["XYZ_Airline"];
        }
        if (airlineResponses.hasKey("PQR_Airline")) {
            results[0] = <string> airlineResponses["PQR_Airline"];
        }
        return results;
    } timeout (30) (map airlineResponses) {
        return results;
    }
}