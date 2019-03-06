const map<boolean> bm1 = { "bm1k": true };
const map<boolean> bm2 = { "bm2kn": bm1.bm2k };

function testInvalidBooleanConstKeyReference() returns map<boolean> {
    return bm2;
}
