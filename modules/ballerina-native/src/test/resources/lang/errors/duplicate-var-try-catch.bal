import ballerina.lang.errors;

function testInvalid (){
    try{
        int a = 10;
    } catch (errors:Error e){
        errors:Error e = { msg: "test"};
        int b = 10;
    }
}
