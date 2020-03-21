function testForkAndWaitForAny() returns string {
        fork {
            @strand{thread:"any"}
            worker ABC_Airline returns string {
                return "abc";
            }
            @strand{thread:"any"}
            worker XYZ_Airline returns string {
                return "xyz";
            }
        }

        return wait ABC_Airline | XYZ_Airline;
}