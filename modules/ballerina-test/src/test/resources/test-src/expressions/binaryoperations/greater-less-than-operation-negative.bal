function checkGreaterThanForUnsupportedType() (boolean){
    json j1;
    json j2;
    j1 = {"name":"Jack"};
    j2 = {"state":"CA"};

    return j1 > j2;
}

function checkGreaterThanEualForUnsupportedType() (boolean){
    json j1;
    json j2;
    j1 = {"name":"Jack"};
    j2 = {"state":"CA"};

    return j1 >= j2;
}


function checkLessThanForUnsupportedType() (boolean){
    json j1;
    json j2;
    j1 = {"name":"Jack"};
    j2 = {"state":"CA"};

    return j1 < j2;
}

function checkLessThanEualForUnsupportedType() (boolean){
    json j1;
    json j2;
    j1 = {"name":"Jack"};
    j2 = {"state":"CA"};

    return j1 <= j2;
}
