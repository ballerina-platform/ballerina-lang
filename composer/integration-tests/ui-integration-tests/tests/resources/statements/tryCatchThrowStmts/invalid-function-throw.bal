function testInvalid (){
    throw funcReturnInt();
}

function funcReturnInt()(int){
    int a = 10;
    return a;
}
