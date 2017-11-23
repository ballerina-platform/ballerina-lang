function testForkJoinAllOfSpecific()(string[]) {

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
            any[] abc;
            any[] xyz;
            abc,_ = (any[]) airlineResponses["ABC_Airline"];
            xyz,_ = (any[]) airlineResponses["XYZ_Airline"];
            results[0], _ = (string) abc[0];
            results[1], _ = (string) xyz[0];
            return results;
        } timeout (30) (map airlineResponses) {
            return results;
        }
}