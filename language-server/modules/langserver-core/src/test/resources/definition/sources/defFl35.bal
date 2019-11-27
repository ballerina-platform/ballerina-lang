public int const CONST_VAL = 1;

function testMatchStatement1() {
    int refVar = 0;
    match getTupleValue() {
        [1, [1, {name: "A"}]] => {
            refVar = 1;
        }
        // List Binding Pattern
        var [iVal1, [iVal2, studentVal]] => {
            int a = iVal2;
        }
        // Variable name binding pattern
        var varVal => {
            int a = varVal[0];
        }
    }
    match getTupleValue() {
        var [iVal1, [iVal2, {age: a, name: n}]] => {
            string name = n;
        }
        // Wildcard binding pattern
        _ => {
            refVar = 2;
        }
    }
    match getTupleValue() {
        // List Binding Pattern
        var [iVal1, [iVal2, {age, name}]] => {
            string n = name;
        }
    }
    match getTupleValue() {
        // List Binding Pattern
        var [iVal1, [iVal2, {age: a, name: n}]] => {
            string name = n;
        }
    }
    match getIntVal() {
        1 => {
            refVar = 1;
        }
    }
}

function testMatchStatement2() {
    int refVal = 0;
    match getMappingValue() {
        {name: "USA", capital: {name: "WDC", code: 1}} => {
            refVal = 1;
        }
        var {name, capital} => {
            string capName = capital.name;
        }
    }

    match getMappingValue() {
        var {name: coName, capital: {name, code: c}} => {
            int capCode = c;
        }
    }
}

function testConstPattern() {
    int refVal = -1;
    match getIntVal() {
        1 => {
            refVal = 1;
        }
        CONST_VAL => {
            refVal = 2;
        }
    }
}

function getIntVal() returns int {
    return 0;
}

function getTupleValue() returns [int, [int, Student]] {
    Student student = {
        age: 20,
        name: "Bob"
    };
    return [1, [2, student]];
}

function getMappingValue() returns Country {
    Country country = {
        name: "USA",
        capital: {
            name: "WDC",
            code: 111
        }
    };

    return country;
}

type Student record {
    int age;
    string name;
};

type Country record {
    string name;
    Capital capital;
};

type Capital record {
    string name;
    int code;
};
