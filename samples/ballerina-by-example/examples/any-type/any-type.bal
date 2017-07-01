import ballerina.lang.system;
import ballerina.doc;

@doc:Description {value:"Global variables defined with type 'any'."}
any stringVal = "string value as any";
any intVal = 6;
any mapAny = {"name":"tom", "age":33};

function main (string[] args) {
    //Using global 'any' variable as function parameter.
    acceptAny(intVal);

    //Initializing struct variable which has 'any' as field types.
    Person person = {name:"john", age:45, email:"abc@cde.com"};

    //Use 'any' field in struct to invoke the function.
    acceptAny(person.age);

    system:println(person.name);

    //Casting a 'map' defined as 'any' to a 'map'.
    var mapVal, _ = (map)mapAny;

    var stringVal, _ = (string)mapVal.name;
    var intVal, _ = (int)mapVal.age;

    system:println("name - " + stringVal);
    system:println("age - " + intVal);
}

@doc:Description {value:"Function accepting 'any' as a parameter value."}
function acceptAny (any param) {
    int value;
    value, _ = (int)param;
    system:println("casted any valu - " + value);
}

@doc:Description {value:"Struct with 'any' as field types."}
struct Person {
    string name;
    any age;
    any email;
}