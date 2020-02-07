import module1;

function getStringInPkg() returns (string){
    map<json> m = {};
    
    foreach (string, int) (k, v) in module1: {
        printMessage();
    }
    
    int b = 12;
}
