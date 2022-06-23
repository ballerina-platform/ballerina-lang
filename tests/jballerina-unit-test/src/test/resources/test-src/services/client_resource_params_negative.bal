type CustomRecord record { string a; };
CustomRecord customRecord = { a: "value"};

public function testClientResourceReturnValuesAndParamArgumentsError() {
    var successClient = client object {
        resource function get stringParamPath(string a) returns string {
            return "string";
        }

        resource function get intParamPath(int a) returns int {
            return 1;
        }

        resource function get booleanParamPath(boolean a) returns boolean {
            return true;
        }

        resource function get floatParamPath(float a) returns float {
            return 1.2f;
        }

        resource function get decimalPath(decimal a) returns decimal {
            return 2.3d;
        }

        resource function get recordParamPath(record {int a;} a) returns record {int a;} {
            return {a: 1};
        }

        resource function get xmlParamPath(xml a) returns xml {
            return xml `<a></a>`;
        }

        resource function get mapParamPath(map<string> a) returns map<string> {
            return {a: "string value"};
        }

        resource function get arrayParamPath(int[] a) returns int[] {
            return [1, 2, 3];
        }

        resource function get customTypeParamPath(CustomRecord a) returns CustomRecord {
            return customRecord;
        }
    };

    xml stringResult = successClient->/stringParamPath("string");
    xml intResult = successClient->/intParamPath(2);
    xml booleanResult = successClient->/booleanParamPath(true);
    xml floatResult = successClient->/floatParamPath(2.3f);
    xml decimalResult = successClient->/decimalPath(3.2d);

    string stringResult2 = successClient->/stringParamPath(2);
    int intResult2 = successClient->/intParamPath("string");
    boolean booleanResult2 = successClient->/booleanParamPath(3.4f);
    float floatResult2 = successClient->/floatParamPath(2.3d);
    decimal decimalResult2 = successClient->/decimalPath(true);

    string recordResult = successClient->/recordParamPath({a: 2});
    string xmlResult = successClient->/xmlParamPath(xml `<a></a>`);
    string mapResult = successClient->/mapParamPath({a: "value"});
    string intArrayResult = successClient->/arrayParamPath([1, 2, 3]);
    string customRecordResult = successClient->/customTypeParamPath(customRecord);

    record {int a;} recordResult2 = successClient->/recordParamPath(customRecord);
    xml xmlResult2 = successClient->/xmlParamPath({a: 2});
    map<string> mapResult2 = successClient->/mapParamPath(xml `<a></a>`);
    int[] intArrayResult2 = successClient->/arrayParamPath({a: "value"});
    CustomRecord customRecordResult2 = successClient->/customTypeParamPath([1, 2, 3]);
}
