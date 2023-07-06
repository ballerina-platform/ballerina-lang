function testFunction() {
    map<int> myMap = {a:10,b:20};
    var result = from [string,int] entry in  myMap.entries()
                     order by entry[1] a
}
