function testMultiReturnVarRefDuplication() returns [int,int] {
    var [age, age] = retTwoInt();
    return [age, age];
}

function retTwoInt() returns [int,int]{
    return [100, 200];
}