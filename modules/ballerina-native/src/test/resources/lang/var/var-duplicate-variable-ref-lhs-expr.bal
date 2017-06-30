function testMultiReturnVarRefDuplication() (int,int) {
    var age, age = retTwoInt();
    return age, age;
}

function retTwoInt()(int,int){
    return 100, 200;
}