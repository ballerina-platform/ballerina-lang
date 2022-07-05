
public function main() {
    record {} x = {};
    xmlns "http://abc.pqr/" as ns;
    xml testXml = xml `<ns:test></ns:test>`;

    match x {
        var {a, b} if getValue(testXml/*) =>  {
        }
        var {a, c} =>  {
        }
    }

    boolean d = true;
    
}

isolated function getValue(xml xmlVal) returns boolean {
    return false;
}
