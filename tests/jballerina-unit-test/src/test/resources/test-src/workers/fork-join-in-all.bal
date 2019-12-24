function testForkAndWaitForAll() returns int[]|error {

    int[] results = [];

    fork {
        @concurrent{}
        worker ABC_Airline returns int {
            int x = 234;
            return x;
        }

        @concurrent{}
        worker XYZ_Airline returns int {
            int x = 500;
            return x;
        }
    }
    map<int> resultRecode = wait {ABC_Airline, XYZ_Airline};
    results[0] = resultRecode["ABC_Airline"] ?: 0;
    results[1] = resultRecode["XYZ_Airline"] ?: 0;
    return results;
}

