function checkEqulalityOfTwoTypes() (boolean){
    int a;
    string b;
    return a == b;
}

function checkEqulalityOfUnsupportedType() (boolean){
    json j1;
    json j2;
    j1 = {"name":"Jack"};
    j2 = {"state":"CA"};

    return j1 == j2;
}

function checkNotEqulalityOfUnsupportedType() (boolean){
    json j1;
    json j2;
    j1 = {"name":"Jack"};
    j2 = {"state":"CA"};

    return j1 != j2;
}
