import ballerina/io;
type Person record {|
   string firstName;
   string lastName;
   int age;
|};

type Teacher record {|
   string firstName;
   string lastName;
|};

type Employee record {|
   string firstName;
   string lastName;
   string department;
   string company;
|};

function testSimpleSelectQueryWithSimpleVariable() returns   Person[]{

     Person p1 = {firstName: "Alex", lastName: "George", age: 23};
         Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
         Person p3 = {firstName: "John", lastName: "David", age: 33};

         Person[] personList = [p1, p2, p3];

          Person[] outputFromQuery = from Person person in personList
                                  select person;

         //Person[]|error? outputList = ();
         //Person[] tempList = [];
         //var iteratorObj = personList.iterator();
         //
         //record {|Person value;|}|error? nextVal = iteratorObj.next();
         //while (true) {
         //    if (nextVal is ()) {
         //        break;
         //    } else if (nextVal is error) {
         //        outputList = nextVal;
         //        break;
         //    } else {
         //        var value = nextVal.value;
         //        tempList[tempList.length()] = value;
         //    }
         //    nextVal = iteratorObj.next();
         //}
         //
         //if (outputList is ()) {
         //    outputList = tempList;
         //}
         //io:println(outputList);
         //
         //return outputList;

     io:println(outputFromQuery);
    return outputFromQuery;
}