connector Calculator(string txt){

    action add1(Calculator jcl, int a, int b) (int) {
        int response=a+b;
        return response;
    }

    action substract1(Calculator jcl, int a, int b) (int) {
        int response=a-b;
        return response;
    }
}

function testAction(string[] args){
    Calculator cal = create Calculator("text");
    int m = cal.add1(cal, 1,2);
}