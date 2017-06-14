function testGetFromNull() (string) {
    json j2 = { age:43, name:null };
    return (string)j2.name.fname;
}

function testAddToNull() (json) {
    json j = {name:"Supun", address:null};
    j.address.country = "SriLanka";
    return j;
}
