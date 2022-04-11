function foo() {
    match x {
        int:MAX_VALUE => {
        }

        [int:MAX_VALUE, error:ERR_VALUE] => {
        }

        {f1: int:MAX_VALUE, f2: error:ERR_VALUE} => {
        }

        error(string:STRING_VAL, error:ERROR_VAL, m = "s") => {
        }
    }
}
