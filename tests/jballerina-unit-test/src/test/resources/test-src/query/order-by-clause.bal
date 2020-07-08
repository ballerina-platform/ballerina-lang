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

function testSimpleQueryExpr() returns Student[] {
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
