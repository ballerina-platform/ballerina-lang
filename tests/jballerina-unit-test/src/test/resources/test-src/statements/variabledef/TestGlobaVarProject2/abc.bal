import globalvarpkg.mod.cde;
import globalvarpkg.mod.efg;


function getStringInPkg() returns (string){
    int val = cde:getSample();
    return efg:getStringVal();
}
