function testNullInForkJoin () returns [string, string]|error {
    string _ = "";
    fork {
        worker foo returns int {
            string _ = "";
        }

        worker bar returns int {
            string _ = "";
        }
    }
}