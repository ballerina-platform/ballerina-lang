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
        if (airlineResponses["ABC_Airline"] != null) {
            any[] abc;
            abc =check <any[]> airlineResponses["ABC_Airline"];
            results[0] = <string>abc[0];
        }
        if (airlineResponses["XYZ_Airline"] != null) {
            any[] xyz;
            xyz =check <any[]> airlineResponses["XYZ_Airline"];
            results[0] = <string> xyz[0];
        }
        if (airlineResponses["PQR_Airline"] != null) {
            any[] pqr;
            pqr =check <any[]> airlineResponses["PQR_Airline"];
            results[0] = <string> pqr[0];
        }
        return results;
    } timeout (30) (map airlineResponses) {
        return results;
    }
}