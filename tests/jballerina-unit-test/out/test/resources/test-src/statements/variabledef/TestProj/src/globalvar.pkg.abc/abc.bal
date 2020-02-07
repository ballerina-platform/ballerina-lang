
import globalvar.pkg.cde;
import globalvar.pkg.efg;


function getStringInPkg() returns (string){
    int val = cde:getSample();
    return efg:getStringVal();
}
