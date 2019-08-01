import module1;

function getStringInPkg() returns (string){
    map<json> m = {};
    
    foreach [string  ] [k, v] in check map<json>.convert(m) {
        printMessage();
    }
    
    int b = 12;
}
