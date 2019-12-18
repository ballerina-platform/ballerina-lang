function testForkAndWaitForAny() returns string {
        fork {
            @concurrent{}
            worker ABC_Airline returns string {
                return "abc";
            }
            @concurrent{}
            worker XYZ_Airline returns string {
                return "xyz";
            }
        }

        return wait ABC_Airline | XYZ_Airline;
}