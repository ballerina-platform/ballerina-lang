import module1;

function getStringInPkg() returns (string){
    int a = 12;
    
    match expr {
        var (s, i) if s is string => 
        var (s, i) => {printMessage();}
        {x: 12, y: "B"} => printMessage();
        12 => printMessage();
    }
    
    int b = 12;
}
