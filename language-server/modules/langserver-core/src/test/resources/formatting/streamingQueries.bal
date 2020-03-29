type Person record {|
    string firstName;
    string lastName;
    string deptAccess;
|};

type FullName record {|
   string firstName;
   string lastName;
|};

type Department record {|
    string name;
|};

type Company record {|
   string name;
|};

type Teacher record {|
   string firstName;
   string lastName;
   int age;
   string teacherId;
|};

type Employee record {|
   string firstName;
   string lastName;
   string department;
   string company;
|};

function testMultipleSelectClausesWithSimpleVariable() returns Person[] {

    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};
    Person p3 = {firstName: "John", lastName: "David", deptAccess: "XYZ"};

    Department d1 = {name: "HR"};
    Department d2 = {name: "Operations"};

          Person[] personList = [p1, p2, p3];
    Department[] deptList = [d1, d2];

    var outputPersonList =
                 from    var   person   in   personList
            from
                 var dept in deptList
        select {firstName: person.firstName,lastName:person.lastName,deptAccess:dept.name};

    return outputPersonList;
}


function testMultipleSelectClausesWithRecordVariable() returns Person[] {

    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};
    Person p3 = {firstName: "John", lastName: "David", deptAccess: "XYZ"};

    Department d1 = {name: "HR"};
    Department d2 = {name: "Operations"};

    Person[] personList = [p1, p2, p3];
    Department[] deptList = [d1, d2];

    var outputPersonList =
        from    var  {firstName: nm1,
            lastName: nm2,
            deptAccess: d}    in   personList
            from
               var  {name: deptName}  in  deptList
               select {firstName: nm1,
            lastName: nm2,
            deptAccess: deptName
        };

    return outputPersonList;
}

function testMultipleSelectClausesWithRecordVariableV2() returns Person[] {

    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};
    Person p3 = {firstName: "John", lastName: "David", deptAccess: "XYZ"};

    Department d1 = {name: "HR"};
    Department d2 = {name: "Operations"};

    Person[] personList = [p1, p2, p3];
    Department[] deptList = [d1, d2];

    var outputPersonList =
               from      var {firstName,
            lastName,
            deptAccess}  in personList
        from
               var { name }  in  deptList
        select {firstName: firstName,
            lastName: lastName,
            deptAccess: name};

    return outputPersonList;
}

function testMultipleSelectClausesWithSimpleVariable1() returns Person[] {

    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};
    Person p3 = {firstName: "John", lastName: "David", deptAccess: "XYZ"};

    Department d1 = {name: "HR"};
    Department d2 = {name: "Operations"};

    Person[] personList = [p1, p2, p3];
    Department[] deptList = [d1, d2];

    Person[] outputPersonList =
        from
                var    person   in    personList
        from     var dept in deptList
        select {
               firstName: person.firstName,
               lastName: person.lastName,
              deptAccess: dept.name
        };

    return outputPersonList;
}


function testMultipleSelectClausesWithRecordVariable1() returns Person[] {

    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};
    Person p3 = {firstName: "John", lastName: "David", deptAccess: "XYZ"};

    Department d1 = {name: "HR"};
    Department d2 = {name: "Operations"};

    Person[] personList = [p1, p2, p3];
    Department[] deptList = [d1, d2];

    Person[] outputPersonList =
        from   var {
               firstName: nm1,
            lastName: nm2,
            deptAccess: d}   in   personList
        from    var   {name: deptName}  in  deptList
         select {
              firstName: nm1,
             lastName: nm2,
            deptAccess: deptName
        };

    return outputPersonList;
}

function testMultipleSelectClausesWithRecordVariableV3() returns Person[] {

    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};
    Person p3 = {firstName: "John", lastName: "David", deptAccess: "XYZ"};

    Department d1 = {name: "HR"};
    Department d2 = {name: "Operations"};

    Person[] personList = [p1, p2, p3];
    Department[] deptList = [d1, d2];

    Person[] outputPersonList =
             from    var   {firstName, lastName, deptAccess}   in   personList
          from     var   {name}   in   deptList
         select {
              firstName:  firstName,
             lastName: lastName,
             deptAccess: name
        };

    return outputPersonList;
}

function testMultipleFromClausesWithStream() returns Person[] {
    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};
    Person p3 = {firstName: "John", lastName: "David", deptAccess: "XYZ"};

    Department d1 = {name: "HR"};
    Department d2 = {name: "Operations"};

    Person[] personList = [p1, p2, p3];
    Department[] deptList = [d1, d2];

    Person[] outputPersonList =
              from   var   {firstName,lastName,deptAccess}   in    personList.toStream()
            from
                var   {name}  in  deptList
          select {
            firstName: firstName,
                   lastName: lastName,deptAccess: name
        };

    return outputPersonList;
}

function testMultipleFromWithLetClauses() returns Person[] {
    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};
    Person p3 = {firstName: "John", lastName: "David", deptAccess: "XYZ"};

    Department d1 = {name: "HR"};
    Department d2 = {name: "Operations"};

    Person[] personList = [p1, p2, p3];
    Department[] deptList = [d1, d2];

    Person[] outputPersonList =
                   from  var  {firstName, lastName, deptAccess} in personList.toStream()
            from
                var   {name}  in deptList
                  let
              string companyName = "WSO2"
            select {
                firstName: firstName,lastName: lastName,
            deptAccess: companyName
        };
    return outputPersonList;
}

function testMultipleLetClausesWithSimpleVariable1() returns Person[] {
    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};
    Person p3 = {firstName: "John", lastName: "David", deptAccess: "XYZ"};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from var person in personList
            let   string  depName = "WSO2"  ,  string replaceName  = "Alexander"
            where person.deptAccess == "XYZ" && person.firstName == "Alex"
            select {
                   firstName: replaceName,
                   lastName: person.lastName,
                   deptAccess: depName
            };
    return  outputPersonList;
}

function testMultipleLetClausesWithSimpleVariable2() returns Person[] {
    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};
    Person p3 = {firstName: "John", lastName: "David", deptAccess: "XYZ"};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from var person in personList
            let
            string depName = "WSO2"
            let
            string replaceName = "Alexander"
            where person.deptAccess == "XYZ" && person.firstName == "Alex"
            select {
                   firstName: replaceName,
                   lastName: person.lastName,
                   deptAccess: depName
            };
    return  outputPersonList;
}

function testMultipleLetClausesWithRecordVariable() returns Person[] {
    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};
    Person p3 = {firstName: "John", lastName: "David", deptAccess: "XYZ"};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from var { firstName: nm1, lastName: nm2, deptAccess: d } in personList
            let
            Company companyRecord = { name: "WSO2" }
            select {
                   firstName: nm1,
                   lastName: nm2,
                   deptAccess: companyRecord.name
            };
    return  outputPersonList;
}

function testMultipleVarDeclReuseLetClause() returns Teacher[] {

    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};

    Person[] personList = [p1, p2];

    var outputPersonList =
            from   var person in personList
            let
            int x = 20,
             int y = x + 10
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   age: y,
                   teacherId: "TER1200"
            };
    return  outputPersonList;
}

function testMultipleWhereClausesWithSimpleVariable() returns Person[]{

    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};
    Person p3 = {firstName: "John", lastName: "David", deptAccess: "XYZ"};

     Department d1 = {name:"HR"};
     Department d2 = {name:"Operations"};

    Person[] personList = [p1, p2, p3];
    Department[] deptList = [d1, d2];

    Person[] outputPersonList =
            from var person in personList
            from var dept in deptList
         where
                  person.firstName == "Alex"
               where
                 person.deptAccess == "XYZ"
               select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   deptAccess: dept.name
            };
    return  outputPersonList;
}

function testMultipleWhereClausesWithRecordVariable() returns Person[]{

    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};
    Person p3 = {firstName: "John", lastName: "David", deptAccess: "XYZ"};

    Department d1 = {name:"HR"};
    Department d2 = {name:"Operations"};

    Person[] personList = [p1, p2, p3];
    Department[] deptList = [d1, d2];

    Person[] outputPersonList =
    from var { firstName: nm1, lastName: nm2, deptAccess: d } in personList
                     from var { name: deptName } in deptList
                 where     nm1    ==   "Alex"
               where     deptName == "Operations"
                select {
                   firstName: nm1,
                   lastName: nm2,
                   deptAccess: deptName
            };
    return  outputPersonList;
}

function testMultipleWhereClausesWithSimpleVariable1() returns Person[]{

    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};
    Person p3 = {firstName: "John", lastName: "David", deptAccess: "XYZ"};

     Department d1 = {name:"HR"};
     Department d2 = {name:"Operations"};

    Person[] personList = [p1, p2, p3];
    Department[] deptList = [d1, d2];

    Person[] outputPersonList =
            from
            var person in personList
            from
             var dept in deptList
    where
                 person.firstName == "Alex"
                  where
                  person.deptAccess == "XYZ"
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   deptAccess: dept.name
            };
    return  outputPersonList;
}


function testMultipleWhereClausesWithRecordVariable1() returns Person[]{

    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};
    Person p3 = {firstName: "John", lastName: "David", deptAccess: "XYZ"};

    Department d1 = {name:"HR"};
    Department d2 = {name:"Operations"};

    Person[] personList = [p1, p2, p3];
    Department[] deptList = [d1, d2];

    Person[] outputPersonList =
            from     var { firstName: nm1, lastName: nm2, deptAccess: d } in personList
                from     var { name: deptName } in deptList
               where
                   nm1  ==  "Alex"
        where     deptName  ==  "Operations"
                select {
                   firstName: nm1,
                   lastName: nm2,
                   deptAccess: deptName
            };
    return  outputPersonList;
}

function testSimpleSelectQueryWithSimpleVariable() returns Person[]{

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from var person in personList
                select {
                      firstName: person.firstName,
                      lastName: person.lastName,
                      age: person.age
            };

    return  outputPersonList;
}

function testSimpleSelectQueryWithRecordVariable() returns Person[]{

    Person p1 = {firstName:"Alex", lastName: "George", age: 23};
    Person p2 = {firstName:"Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName:"John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
                   from     var { firstName: nm1, lastName: nm2, age: a }      in personList
             select {
                      firstName: nm1,
                         lastName: nm2,
                     age: a
                };

    return  outputPersonList;
}

function testSimpleSelectQueryWithRecordVariableV2() returns Person[]{

    Person p1 = {firstName:"Alex", lastName: "George", age: 23};
    Person p2 = {firstName:"Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName:"John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
                   from var { firstName, lastName, age } in personList
            select {  firstName: firstName  ,lastName: lastName , age: age};

    return  outputPersonList;
}

function testSimpleSelectQueryWithRecordVariableV3() returns Teacher[]{

    Person p1 = {firstName:"Alex", lastName: "George", age: 23};
    Person p2 = {firstName:"Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName:"John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Teacher[] outputPersonList =
                   from    var    { firstName , lastName, age }  in  personList
              select {
                      firstName   ,
                         lastName
               };

    return  outputPersonList;
}

function testSimpleSelectQueryWithWhereClause() returns Person[]{

    Person p1 = {firstName:"Alex", lastName: "George", age: 23};
    Person p2 = {firstName:"Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName:"John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
                  from
                         var person in personList
            where
                    person.age >= 30
            select    {
                       firstName: person.firstName,
                      lastName: person.lastName,
  age: person.age
                };
    return  outputPersonList;
}

function testQueryExpressionForPrimitiveType() returns int[]{

    int[] intList = [12, 13, 14, 15, 20, 21, 25];

          int[] outputIntList =
                   from  var  value  in  intList
              where   value  >  20
     select     value   ;

    return  outputIntList;
}

function testQueryExpressionWithSelectExpression() returns string[]{

    int[] intList = [1, 2, 3];

    string[] stringOutput =
                from
                      var value in intList
               select     value  . toString() ;

    return  stringOutput;
}

function testFilteringNullElements() returns Person[] {

    Person p1 = {firstName:"Alex", lastName: "George", age: 23};
    Person p2 = {firstName:"Ranjan", lastName: "Fonseka", age: 30};

    Person?[] personList = [p1,  (), p2];

    Person[] outputPersonList =
                      from    var   person   in   personList
                           where   (person is Person)
                                select {
                             firstName: person.firstName,
                            lastName: person.lastName,
                          age: person.age
                         };
    return outputPersonList;
}

function testMapWithArity () returns string[] {
    map<any> m = {a:"1A", b:"2B", c:"3C", d:"4D"};
    string[] val = from    var  v  in  m
                      where    < string >  v  ==   "1A"
                      select     < string >   v ;
    return val;
}

function testJSONArrayWithArity() returns string[] {
    json[] jdata = [{ name : "bob", age : 10}, { name : "tom", age : 16}];
    string[] val =  from  var  v  in  jdata
                       select    < string >   v . name;
    return val;
}

function testArrayWithTuple() returns string[] {
    [int, string][] arr = [[1, "A"], [2, "B"], [3, "C"]];
    string[] val =  from  var  [i, v]  in arr
                    where  i  ==  3
                   select  v ;
    return val;
}

function testFromClauseWithStream() returns Person[]{
    Person p1 = {firstName: "Alex", lastName: "George", age: 30};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 40};
    Person p3 = {firstName: "John", lastName: "David", age: 50};

    Person[] personList = [p1, p2, p3];
       stream < Person > streamedPersons = personList.toStream();

    Person[] outputPersonList =
                   from      var  person  in  streamedPersons
            where    person.age  ==  40
                select    person;
    return    outputPersonList;
}

function testSimpleSelectQueryWithLetClause() returns Employee[] {

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Employee[] outputPersonList =
                from  var  person  in  personList
               let   string   depName  =  "HR"  ,  string  companyName  =  "WSO2"
               where    person.age  >=  30
                select {
                     firstName: person.firstName,
                     lastName: person.lastName,
                   department: depName,
                   company: companyName
             };
    return  outputPersonList;
}

function testFunctionCallInVarDeclLetClause() returns Person[]{

   Person p1 = {firstName: "Alex", lastName: "George", age: 23};
   Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};

   Person[] personList = [p1, p2];

    var outputPersonList =
                from  Person  person  in  personList
                 let   int  twiceAge =  mutiplyBy2 ( person . age )
               select {
                      firstName: person.firstName,
                     lastName: person.lastName,
                      age: twiceAge
               };

    return  outputPersonList;
}

function testUseOfLetInWhereClause() returns Person[]{

   Person p1 = {firstName: "Alex", lastName: "George", age: 18};
   Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 22};

   Person[] personList = [p1, p2];

    var outputPersonList =
                 from  var  person  in   personList
               let   int   twiceAge  =  mutiplyBy2(person.age)
               where    twiceAge > 40
               select {
                    firstName: person.firstName,
                     lastName: person.lastName,
                      age: twiceAge
               };

    return  outputPersonList;
}

function mutiplyBy2 (int k) returns int {
    return k * 2;
}

function testSimpleSelectQueryWithSimpleVariable() returns Person[]{
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
               from  var  person  in  personList
              select  {
                   firstName: person.firstName,
                      lastName: person.lastName,
                   age: person.age
            };

    return  outputPersonList;
}

function testSimpleSelectQueryWithRecordVariable() returns Person[]{
    Person p1 = {firstName:"Alex", lastName: "George", age: 23};
    Person p2 = {firstName:"Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName:"John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
                  from  var  { firstName: nm1, lastName: nm2, age: a }  in  personList
select {
                    firstName: nm1,
                       lastName: nm2,
                    age: a
            };

    return  outputPersonList;
}

function testSimpleSelectQueryWithRecordVariableV2() returns Person[]{
    Person p1 = {firstName:"Alex", lastName: "George", age: 23};
    Person p2 = {firstName:"Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName:"John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    var outputPersonList =
                from    var  { firstName , lastName,   age  }   in   personList
            select {
                    firstName  : firstName,
                   lastName: lastName,
                   age: age
            };

    return  outputPersonList;
}

function testSimpleSelectQueryWithRecordVariableV3() returns Person[]{
    Teacher p1 = {firstName:"Alex", lastName: "George", age: 23, teacherId: "XYZ01"};
    Teacher p2 = {firstName:"Ranjan", lastName: "Fonseka", age: 30, teacherId: "ABC01"};
    Teacher p3 = {firstName:"John", lastName: "David", age: 33, teacherId: "ABC10"};

    Teacher[] teacherList = [p1, p2, p3];

    var outputPersonList =
            from   var   {   firstName , lastName, age , teacherId}   in   teacherList
              select  {
                       firstName: firstName,
                   lastName: lastName,
                   age: age
            };

    return  outputPersonList;
}

function testSimpleSelectQueryWithWhereClause() returns Person[]{
    Person p1 = {firstName:"Alex", lastName: "George", age: 23};
    Person p2 = {firstName:"Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName:"John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    var outputPersonList =
               from   var   person   in   personList
                  where     person.age  >=  30
                select    {
                      firstName : person.firstName,
                       lastName : person.lastName,
                        age : person.age
            };
    return  outputPersonList;
}

function testQueryExpressionForPrimitiveType() returns int[]{

    int[] intList = [12, 13, 14, 15, 20, 21, 25];

    var outputIntList =
              from  var  value  in  intList
                where    value  >  20
               select    value;

    return  outputIntList;
}

function testQueryExpressionWithSelectExpression() returns string[]{

    int[] intList = [1, 2, 3];

    var stringOutput =
               from   var   value  in  intList
                select    value . toString() ;

    return  stringOutput;
}

function testFilteringNullElements() returns Person[] {

    Person p1 = {firstName:"Alex", lastName: "George", age: 23};
    Person p2 = {firstName:"Ranjan", lastName: "Fonseka", age: 30};

    Person?[] personList = [p1,  (), p2];

    var outputPersonList =
                          from var person in personList
                      where (person is Person)
                       select {
                         firstName: person.firstName,
                         lastName: person.lastName,
                         age: person.age
                         };
    return outputPersonList;
}

function testMapWithArity () returns string[] {
    map<any> m = {a:"1A", b:"2B", c:"3C", d:"4D"};
    var val =from   var  v  in  m
                   where  <string>  v  ==  "1A"
                     select   <string>  v ;
    return val;
}

function testJSONArrayWithArity() returns string[] {
    json[] jdata = [{ name : "bob", age : 10}, { name : "tom", age : 16}];
    var val =   from  var  v  in  jdata
                   select   <string>  v .name;
    return val;
}

function testArrayWithTuple() returns string[] {
    [int, string][] arr = [[1, "A"], [2, "B"], [3, "C"]];
    var val =   from   var [ i , v ]  in  arr
                        where  i ==  3
                    select  v ;
    return val;
}

function testQueryExpressionWithVarType() returns Teacher[]{

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    var outputPersonList =
               from   var  person  in  personList
             select {
                    firstName : person.firstName,
                     lastName : person.lastName,
                      age : person.age,
                      teacherId : "TER1200"
               };

    return  outputPersonList;
}

function testSimpleSelectQueryWithSpreadOperator() returns Person[]{
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
                from    var   person  in  personList
                select {
                      ...person
            };

    return  outputPersonList;
}

function testQueryExpressionWithSpreadOperatorV2() returns Teacher[]{

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    var outputPersonList =
            from    var    person  in  personList
               select  {
                        ...person,
                    teacherId: "TER1200"
            };

    return  outputPersonList;
}

function testSimpleQueryAction() returns FullName[]{

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];
    FullName[] nameList = [];

    var x =  from    var    person    in    personList   do {FullName fullName = {firstName: person.firstName, lastName: person.lastName};nameList[nameList.length()] = fullName;};

    return nameList;
}

function testSimpleQueryAction2() returns int{

    int[] intList = [1, 2, 3];
    int count = 0;

    var x =    from    var     value in intList
                 do{};

    return count;
}

function testSimpleQueryActionWithRecordVariable() returns FullName[]{

    Person p1 = {firstName:"Alex", lastName: "George", age: 23};
    Person p2 = {firstName:"Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName:"John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];
    FullName[] nameList = [];

    var x =      from
     var { firstName: nm1, lastName: nm2, age: a } in personList
                do {
                    FullName fullName = {firstName: nm1, lastName: nm2};
                   nameList[nameList.length()] = fullName;
                };

    return  nameList;
}

function testSimpleSelectQueryWithRecordVariableV2() returns FullName[]{

    Person p1 = {firstName:"Alex", lastName: "George", age: 23};
    Person p2 = {firstName:"Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName:"John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];
    FullName[] nameList = [];

    var x =      from    var   {   firstName, lastName, age }  in  personList
                do      {
    FullName fullName = {firstName: firstName, lastName: lastName};
                       nameList[nameList.length()] = fullName;
                  };

    return  nameList;
}

function testSimpleSelectQueryWithLetClause() returns  FullName[] {
    Person p1 = {firstName:"Alex", lastName: "George", age: 23};
    Person p2 = {firstName:"Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName:"John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];
    FullName[] nameList = [];

    var x =     from    var person in personList
                   let    int    twiceAge  = (person.age * 2)
                 do {
                     if(twiceAge < 50) {
                       FullName fullName = {firstName: person.firstName, lastName: person.lastName};
                    nameList[nameList.length()] = fullName;
                   }

                  };
    return  nameList;
}

function testSimpleSelectQueryWithWhereClause() returns  FullName[] {
    Person p1 = {firstName:"Alex", lastName: "George", age: 23};
    Person p2 = {firstName:"Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName:"John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];
    FullName[] nameList = [];

    var x =      from    var    person  in  personList
               where    (person.age * 2)   < 50
   do {FullName fullName = {firstName: person.firstName, lastName: person.lastName};nameList[nameList.length()] = fullName;};
    return  nameList;
}

function testSimpleSelectQueryWithMultipleFromClauses() returns  Employee[] {
    Person p1 = {firstName:"Alex", lastName: "George", age: 23};
    Person p2 = {firstName:"Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName:"John", lastName: "David", age: 33};

    Department d1 = {name:"HR"};
    Department d2 = {name:"Operations"};

    Person[] personList = [p1, p2, p3];
    Department[] deptList = [d1, d2];
    Employee[] employeeList = [];

    var x =    from
    var person in personList
               from
               var dept in deptList
                   let
                   string hrDepartment = "Human Resource"
                do {
                   if(dept.name == "HR") {
                       Employee employee = {firstName: person.firstName, lastName: person.lastName, deptAccess: hrDepartment};
                       employeeList[employeeList.length()] = employee;
                    }
                };
    return  employeeList;
}