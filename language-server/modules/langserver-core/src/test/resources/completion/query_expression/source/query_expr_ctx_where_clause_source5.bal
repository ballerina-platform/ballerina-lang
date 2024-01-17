type recType record {|
    int i;
    string s;
|};

type Person record {|
    string firstName;
    string lastName;
    int age;
|};

function testFunction() {
    Person leaderA = {firstName: "Amal", lastName: "Perera", age: 33};
    Person leaderB = {firstName: "Manel", lastName: "Fernando", age: 35};

    Person memberA1 = {firstName: "Nimal", lastName: "Perera", age: 23};
    Person memberA2 = {firstName: "Kamal", lastName: "Perera", age: 26};

    Person memberB1 = {firstName: "Namal", lastName: "Fernando", age: 19};
    Person memberB2 = {firstName: "Rohitha", lastName: "Fernando", age: 29};

    Person[] leaderList = [leaderA, leaderB];
    Person[] memberList = [memberA1, memberA2, memberB1, memberB2];

    var _ = from var leader in leaderList
        let int minLeaderAge = 25
        where 
            (from var member in memberList
            let int memberAge = member.age, int minMemberAge = 20
            where memberAge > 
            select memberAge).pop() >  minLeaderAge
        select leader;
}
