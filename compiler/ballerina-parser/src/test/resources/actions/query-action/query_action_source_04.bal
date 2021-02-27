function foo() {
    var x = from var person in personList
            where (person.age * 2) < 50
            do {
                FullName fullName = {
                    firstName: person.firstName,
                    lastName: person.lastName
                };
                nameList[nameList.length()] = fullName;
            };
}
