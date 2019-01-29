
import c.d;

function testMapInitWithPackageVars() returns (map<any>) {
    map<any> m = { name: d:PI_NAME, value: d:PI_VALUE};
    return m;
}
