// syntax errors
function func1() {
    string var1 = "";
    match var1 {
        v
    }
}

function func2() {
    string v = "";
    match v
}

function func3() {
    string v = "";
    match
}

function func4(any v) {
    match v {
        {a} => {}
    }
}

function func5(any v) {
    match v {
        => {
            
        }
        if true => {
                    
        }
    }
}
