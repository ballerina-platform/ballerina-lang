function testGetFromNull() (string) {
    json j2 = { age:43, name:null };
    string value;
    value, _ = (string)j2.name.fname;
    return value;
}

function testAddToNull() (json) {
    json j = {name:"Supun", address:null};
    j.address.country = "SriLanka";
    return j;
}
