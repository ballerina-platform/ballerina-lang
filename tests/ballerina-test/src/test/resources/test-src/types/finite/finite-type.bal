type State "on"|"off";

function finiteAssignmentStateType() returns State {
    State p = "on";
    State comparator = "on";
    if (p == comparator) {
       p = "off";
    }
    return p;
}

type NumberSet 1|2|3|4|5;

function finiteAssignmentNumberSetType() returns NumberSet {
    NumberSet n = 1;
    NumberSet comparator = 1;
    if (n == comparator) {
       n = 5;
    }
    return n;
}

type StringOrInt int|string;

function finiteAssignmentStringOrIntSetType() returns StringOrInt {
    StringOrInt si = 1;
    StringOrInt comparator = 1;
    if (si == comparator) {
       si = "This is a string";
    }
    return si;
}

function finiteAssignmentStringOrIntSetTypeCaseTwo() returns StringOrInt {
    StringOrInt si = "This is a string";
    StringOrInt comparator = "This is a string";
    if (si == comparator) {
       si = 111;
    }
    return si;
}

type Int int;

function finiteAssignmentIntSetType() returns Int {
    Int si = 1;
    Int comparator = 1;
    if (si == comparator) {
       si = 222;
    }
    return si;
}

function finiteAssignmentIntArrayType() returns Int {
    Int[] si = [];
    si[0] = 10001;
    si[1] = 2345;
    Int comparator = 2345;
    if (si[1] == comparator){
        si[1] = 9989;
    }
    return si[1];
}

function finiteAssignmentStateSameTypeComparison() returns int {
    State a = "off";
    State b = "on";
    if (a == b){
       return 1;
    }
    return 2;
}

function finiteAssignmentStateSameTypeComparisonCaseTwo() returns State {
    State a = "off";
    State b = "on";
    if (a != b){
       a = b;
       return a;
    }
    return b;
}

type POrInt Person|int;

type Person {
   string name;
};

function finiteAssignmentRefValueType() returns POrInt {
    Person p = {name:"abc"};
    POrInt pi = p;
    return pi;
}

function finiteAssignmentRefValueTypeCaseTwo() returns POrInt {
    Person p = {name:"abc"};
    POrInt pi = 4;
    return pi;
}

type PreparedResult "ss"|"sss"|"qqq";

function testFiniteTypeWithMatch() returns PreparedResult {
    match foo() {
         PreparedResult x => return x;
         () => return "qqq";
         error => return "qqq";
    }
}

function foo() returns PreparedResult | error | () {
       PreparedResult x = "ss";
       return x;
}


function testFiniteTypesWithDefaultValues() returns State {
   return assignFiniteValueAsDefaultParam();
}

function assignFiniteValueAsDefaultParam(State cd = "on") returns State {
   Channel c = new(b = cd);
   return c.b ?: "off";
}

type Channel object {

    public {
      State? b,
    }

    new (b = "off", boolean a = true){
        State o =  "on";
        if(b == o) {
           int i = 4;
        }
    }
};

type CombinedState "on"|"off"|int;

function testFiniteTypesWithUnion() returns CombinedState {
   CombinedState abc = 1;
   return abc;
}

function testFiniteTypesWithUnionCaseOne() returns CombinedState {
   CombinedState abc = "off";
   if( abc == "off"){
       return 100;
   }
   return 0;
}

function testFiniteTypesWithUnionCaseTwo() returns CombinedState {
   CombinedState abc = "off";
   if( abc == "off"){
       return "on";
   }
   return "off";
}

function testFiniteTypesWithUnionCaseThree() returns int {
   CombinedState abc = "off";
   if( abc == "off"){
       return 1001;
   }
   return 1002;
}

