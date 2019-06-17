function testNullInForkJoin () returns [string, string] {
    string m = "";
    fork {
        worker foo returns int {
            string resp1 = "";
        }

        worker bar {
            string resp2 = "";
        }
    }
}
