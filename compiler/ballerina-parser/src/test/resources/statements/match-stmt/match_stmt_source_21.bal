function foo() {
    match v {
        {f: a,  } => {
        }

        {f: a, ...var c,} => {
        }

        {...var c,} => {
        }

        {...var c,f:a,var d,f:b} => {
        }

        {f: a, ...var c,f:a,var d,f:b} => {
        }

        {,} => {
        }
    }
}
