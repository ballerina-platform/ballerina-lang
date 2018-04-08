function testForkJoinAllOfSpecific() returns string[] {

        string[] results = [];
        fork {
            worker ABC_Airline {
                "abc" -> fork;
            }

            worker XYZ_Airline {
                "xyz" -> fork;
            }

            worker PQR_Airline {
                "pqr" -> fork;
            }
        } join (all ABC_Airline, XYZ_Airline) (map airlineResponses) {
            string abc= <string> airlineResponses["ABC_Airline"];
            string xyz= <string> airlineResponses["XYZ_Airline"];
            results[0] = abc;
            results[1] = xyz;
            return results;
        } timeout (30) (map airlineResponses) {
            return results;
        }
}