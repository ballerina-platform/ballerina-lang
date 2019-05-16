import module1;

function getStringInPkg() returns (string){
    int a = 12;
    
    transaction with r {
        int c = 12;
    }
    onretry {
        int d = 12;
    }
    committed {
        int e = 12;
    }
    aborted {
        int f = 12;
    }
    
    int b = 12;
}
