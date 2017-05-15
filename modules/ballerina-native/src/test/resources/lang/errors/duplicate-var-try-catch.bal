import ballerina.lang.error;

function testInvalid (){
    try{
        int a = 10;
    } catch (error:error e){
        error:error e = { msg: "test"};
        int b = 10;
    }
}
