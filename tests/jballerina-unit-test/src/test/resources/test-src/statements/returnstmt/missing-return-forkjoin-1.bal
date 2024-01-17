function testNullInForkJoin () returns [string, string] {
    string _ = "";
    fork {
        worker foo returns int {
            string _ = "";
        }

        worker bar {
            string _ = "";
        }
    }
}
