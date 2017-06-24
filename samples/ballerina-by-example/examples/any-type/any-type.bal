import ballerina.lang.system;

@doc:Description {value:"Global level variables defined as 'any'."}
any stringVal = "string value as any";
any intVal = 6;
any jsonAny = {"name":"tom", "age":33};

function main (string[] args) {
    //Using global level 'any' variable as function parameter.
    acceptAny(intVal);

    //Initializing struct variable which has 'any' as field types.
    Person person = {name:"john", age:45, email:"abc@cde.com"};

    //Use 'any' field in struct to invoke the function.
    acceptAny(person.age);

    system:println(person.name);

    //Casting a 'json' defined as 'any' to a 'json'.
    json jsonVal = (json)jsonAny;

    system:println("name - " + (string)jsonVal.name);
    system:println("age - " + (int)jsonVal.age);
}

@doc:Description {value:"Function accepting 'any' as a parameter value."}
function acceptAny (any param) {
    int value;
    value = (int)param;
    system:println("casted any valu - " + value);
}

@doc:Description {value:"Struct with 'any' as field types."}
struct Person {
    string name;
    any age;
    any email;
}