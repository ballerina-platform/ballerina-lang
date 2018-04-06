function testNestedMapAccess() returns (string) {
    map address = {country:"SriLanka"};
    map info = {name:"Supun", address:address};
    return info.address.country;
}
