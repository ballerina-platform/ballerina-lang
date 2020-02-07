import module1;

function getStringInPkg() returns (string){
    module1:TypeX[] a = 12;
    
    foreach module1: v in a {
        printMessage();
    }
    
    int b = 12;
}
