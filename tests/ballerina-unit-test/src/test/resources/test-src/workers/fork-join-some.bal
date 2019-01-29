function testForkAndWaitForAny() returns string {
        fork {
            worker ABC_Airline returns string {
                return "abc";
            }
            worker XYZ_Airline returns string {
                return "xyz";
            }
        }

        return wait ABC_Airline | XYZ_Airline;
}