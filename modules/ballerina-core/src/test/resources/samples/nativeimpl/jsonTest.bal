function getString(json msg) (string){
    string jsonPath;
    jsonPath = "$.name.fname";
    return ballerina.lang.json:getString( msg , jsonPath );
}

function getInt(json msg) (string){
    string jsonPath;
    jsonPath = "$.age";
    return ballerina.lang.json:getInt( msg , jsonPath );
}

function getJson(json msg) (string){
    string jsonPath;
    jsonPath = "$.name";
    return ballerina.lang.json:getJson( msg , jsonPath );
}

function setString(json msg, string value) (string){
    string jsonPath;
    jsonPath = "$.name.fname";
    ballerina.lang.json:set( msg , jsonPath , value );
    return ballerina.lang.json:getString( msg , jsonPath );
}

function addStringToObject(json msg,  string key, string value) (json){
    string jsonPath;
    jsonPath = "$.name";
    ballerina.lang.json:add( msg , jsonPath , key, value );
    return msg;
}

function addStringToArray(json msg, string value) (json){
    string jsonPath;
    jsonPath = "$.users";
    ballerina.lang.json:add( msg , jsonPath , value );
    return msg;
}

function remove(json msg) (json){
    string jsonPath;
    jsonPath = "$.name";
    ballerina.lang.json:remove( msg , jsonPath);
    return msg;
}

function rename(json msg, string oldKey, string newKey) (string){
    string jsonPath;
    jsonPath = "$.name";
    ballerina.lang.json:rename( msg , jsonPath, oldKey, newKey);
    jsonPath = "$.name.firstName";
    return ballerina.lang.json:getString( msg , jsonPath );
}


