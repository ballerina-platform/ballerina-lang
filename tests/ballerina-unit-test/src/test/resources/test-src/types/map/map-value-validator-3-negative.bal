function testMapNegative() returns (int){
    map<any> list1 = {"item1": 1, "item2": 2, "item3": 3, "item4": 4};
    int value = list1["item1"] + 2;
    return value;
}
