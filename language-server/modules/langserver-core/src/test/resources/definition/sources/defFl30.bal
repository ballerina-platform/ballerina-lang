type Country record {
    string name;
    Capital capital;
};

type Capital record {|
    string name;
|};

type Student record {|
    string name;
    int...;
|};

function getCapital() returns Capital {
    Capital capital = {
        name: "Kotte"
    };
    
    return capital;
}

function getIntValue() returns int {
    return 1;
}

public client class SimpleClient {
    public remote function simpleAction() returns int {
        return 1;
    }
}

function testAssignmentStatement1() {
    SimpleClient simpleC = new();
    
    Country country = {
        name: "",
        capital: {
            name: ""
        }
    };
    int[] intArr = [];
    int intVar;
    
    // Assignment
    intVar = getIntValue(); // Expression
    intVar = simpleC->simpleAction(); // Action
    country.capital = getCapital(); // Field Access
    intArr[getIntValue()] = 1; // Member Access
    
    // Compound Assignment
    intVar += getIntValue();
}
