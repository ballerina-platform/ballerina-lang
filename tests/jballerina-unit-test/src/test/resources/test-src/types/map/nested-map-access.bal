function testNestedMapAccess() returns (string) {
    map<any> address = {country:"SriLanka"};
    map<any> info = {name:"Supun", address:address};
    return info["address"]["country"];
}
