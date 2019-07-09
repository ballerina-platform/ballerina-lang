function testNullInForkJoin () returns [string, string]|error {
    string m = "";
    fork {
        worker foo returns int {
            string resp1 = "";
        }

        worker bar returns int {
            string resp2 = "";
        }
    }
}