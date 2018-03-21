function testNestedMapAccess() returns (string) {
    map info = {name:"Supun", address:{country:"SriLanka"}};
    return info.address.country;
}
