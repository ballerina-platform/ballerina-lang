
import c.d;

function testMapInitWithPackageVars() returns (map) {
    map m = { name: d:PI_NAME, value: d:PI_VALUE};
    return m;
}
