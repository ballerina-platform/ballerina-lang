function testInvalid (){
    try{
        int a = 10;
    } catch (error e){
        error e = { msg: "test"};
        int b = 10;
    }
}
