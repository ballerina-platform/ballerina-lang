
import configRecordType.imported_records;
import testOrg/configLib.util;
// import configRecordTypes.default_value_records as def_records;

public function main() {
    testRecords();
    // testTables();
    // testArrays();
    // testMaps();
    testComplexRecords();

    imported_records:testRecords();
    // imported_records:testTables();
    // imported_records:testArrays();
    // imported_records:testMaps();
  
    util:print("Tests passed");
}
