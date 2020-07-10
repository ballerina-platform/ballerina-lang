type Person record {|
   int id;
   string fname;
   Lname lname;
   Address[] addrList;
   map<float> tokens;
   boolean isUndergrad;
|};

type Address record {|
   string addr;
|};

type Lname record {|
   string lname;
|};

type Student record {|
   int pid;
   string firstname;
   Lname lastname;
   Address[] addressList;
   map<float> userTokens;
   boolean isUndergrad;
|};

type Person2 record {|
    string firstName;
    string lastName;
    int age;
|};

type Customer record {|
    readonly int id;
    readonly string name;
    int noOfItems;
|};

type CustomerTable table<Customer> key(id, name);

type CustomerValue record {|
  Customer value;
|};

type PersonValue record {|
    Person2 value;
|};

function getCustomer(record {| Customer value; |}? returnedVal) returns Customer? {
    if (returnedVal is CustomerValue) {
       return returnedVal.value;
    } else {
       return ();
    }
}

function getPersonValue((record {| Person2 value; |}|error?)|(record {| Person2 value; |}?) returnedVal)
returns PersonValue? {
    var result = returnedVal;
    if (result is PersonValue) {
        return result;
    } else {
        return ();
    }
}

function testQueryExprWithOrderByClause() returns Student[] {
    Person p1 = {id: 4, fname: "Zander", lname: {lname: "George"}, addrList: [{addr: "B"}, {addr: "A"}],
              tokens: {one:1, two:2, three:3}, isUndergrad: true};
    Person p2 = {id: 6, fname: "Ranjan", lname: {lname: "Fonseka"}, addrList: [{addr: "C"}, {addr: "B"}],
              tokens: {one:1, two:2, three:3}, isUndergrad: false};
    Person p3 = {id: 4, fname: "Nina", lname: {lname: "Frost"}, addrList: [{addr: "A"}, {addr: "B"}],
              tokens: {one:1, two:2, three:3}, isUndergrad: true};
    Person p4 = {id: 6, fname: "Sanjiva", lname: {lname: "Weeravarana"}, addrList: [{addr: "C"}, {addr: "B"}],
              tokens: {one:2, two:3, three:3}, isUndergrad: false};
    Person p5 = {id: 6, fname: "Sanjiva", lname: {lname: "Herman"} , addrList: [{addr: "C"}, {addr: "B"}],
              tokens: {one:1, two:2, three:3}, isUndergrad: true};
    Person p6 = {id: 6, fname: "Sanjiva", lname: {lname: "Weeravarana"}, addrList: [{addr: "C"}, {addr: "A"}],
              tokens: {one:1, two:2, three:3}, isUndergrad: false};
    Person p7 = {id: 4, fname: "Xyla", lname: {lname: "George"}, addrList: [{addr: "B"}, {addr: "A"}],
              tokens: {one:1, two:2, three:3}, isUndergrad: false};
    Person p8= {id: 6, fname: "Sanjiva", lname: {lname: "Weeravarana"}, addrList: [{addr: "C"}, {addr: "B"}],
              tokens: {one:2, two:3, three:3}, isUndergrad: true};
    Person p9= {id: 6, fname: "Sanjiva", lname: {lname: "Weeravarana"}, addrList: [{addr: "C"}, {addr: "B"}],
              tokens: {one:2, two:3, three:2}, isUndergrad: false};
    Person p10= {id: 6, fname: "Sanjiva", lname: {lname: "Weeravarana"}, addrList: [{addr: "C"}, {addr: "B"}],
              tokens: {one:2, two:2, three:5}, isUndergrad: true};
    Person p11 = {id: 4, fname: "Nina", lname: {lname: "Frost"}, addrList: [{addr: "A"}, {addr: "B"}],
              tokens: {one:1, two:2, three:3}, isUndergrad: false};

    Person[] personList = [p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11];

    Student[] studentList =
       from var person in personList
       order by pid descending, lastname ascending, addressList ascending, userTokens descending, firstname descending
       select {
           pid: person.id,
           firstname : person.fname,
           lastname : person.lname,
           addressList : person.addrList,
           userTokens : person.tokens,
           isUndergrad : person.isUndergrad
       };

    return studentList;
}

function testQueryExprWithLetWhereAndOrderByClauses() returns Student[] {
    Person p1 = {id: 4, fname: "Zander", lname: {lname: "George"}, addrList: [{addr: "B"}, {addr: "A"}],
              tokens: {one:1, two:2, three:3}, isUndergrad: true};
    Person p2 = {id: 3, fname: "Ranjan", lname: {lname: "Fonseka"}, addrList: [{addr: "C"}, {addr: "B"}],
              tokens: {one:1, two:2, three:3}, isUndergrad: false};
    Person p3 = {id: 5, fname: "Nina", lname: {lname: "Frost"}, addrList: [{addr: "A"}, {addr: "B"}],
              tokens: {one:1, two:2, three:3}, isUndergrad: true};
    Person p4 = {id: 1, fname: "Sanjiva", lname: {lname: "Weeravarana"}, addrList: [{addr: "C"}, {addr: "B"}],
              tokens: {one:2, two:3, three:3}, isUndergrad: false};
    Person p5 = {id: 5, fname: "Sanjiva", lname: {lname: "Herman"} , addrList: [{addr: "C"}, {addr: "B"}],
              tokens: {one:1, two:2, three:3}, isUndergrad: true};


    Person[] personList = [p1, p2, p3, p4, p5];

    Student[] studentList =
       from var person in personList
       let boolean status = false
       where person.id > 3
       order by pid, lastname descending
       select {
           pid: person.id,
           firstname : person.fname,
           lastname : person.lname,
           addressList : person.addrList,
           userTokens : person.tokens,
           isUndergrad : status
       };

    return studentList;
}

function testQueryExprWithOrderByClauseReturnTable() returns boolean {
    boolean testPassed = true;
    error onConflictError = error("Key Conflict", message = "cannot insert.");

    Customer c1 = {id: 1, name: "Melina", noOfItems: 12};
    Customer c2 = {id: 2, name: "James", noOfItems: 5};
    Customer c3 = {id: 3, name: "James", noOfItems: 25};
    Customer c4 = {id: 4, name: "James", noOfItems: 25};

    Person2 p1 = {firstName: "Amy", lastName: "Melina", age: 23};
    Person2 p2 = {firstName: "Frank", lastName: "James", age: 30};

    Customer[] customerList = [c1, c2, c3, c4];
    Person2[] personList = [p1, p2];

    CustomerTable|error customerTable = table key(id, name) from var customer in customerList
         from var person in personList
         where person.firstName == "Frank"
         order by noOfItems descending, id
         select {
             id: customer.id,
             name: customer.name,
             noOfItems: customer.noOfItems
         }
         limit 3;


    if (customerTable is CustomerTable) {
        var itr = customerTable.iterator();
        Customer? customer = getCustomer(itr.next());
        testPassed = testPassed && customer == customerList[2];
        customer = getCustomer(itr.next());
        testPassed = testPassed && customer == customerList[0];
        customer = getCustomer(itr.next());
        testPassed = testPassed && customer == customerList[1];
        customer = getCustomer(itr.next());
        testPassed = testPassed && customer == ();
    }

    return testPassed;
}

function testQueryExprWithOrderByClauseReturnStream() returns boolean {
    boolean testPassed = true;

    Person2 p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person2 p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person2 p3 = {firstName: "John", lastName: "David", age: 33};
    Person2 p4 = {firstName: "John", lastName: "Fonseka", age: 28};

    Person2[] personList = [p1, p2, p3, p4];

    stream<Person2> outputPersonStream = stream from var person in personList
        where person.firstName == "John"
        let int newAge = 34
        order by lastName descending
        select {
            firstName: person.firstName,
            lastName: person.lastName,
            age: newAge
        };

    record {| Person2 value; |}? person = getPersonValue(outputPersonStream.next());
    testPassed = testPassed && person?.value?.firstName == "John" && person?.value?.lastName == "Fonseka" &&
    person?.value?.age == 34;

    person = getPersonValue(outputPersonStream.next());
    testPassed = testPassed && person?.value?.firstName == "John" && person?.value?.lastName == "David" &&
    person?.value?.age == 34;

    person = getPersonValue(outputPersonStream.next());
    testPassed = testPassed && person == ();

    return testPassed;
}
