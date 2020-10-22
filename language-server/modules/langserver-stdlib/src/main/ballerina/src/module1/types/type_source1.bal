
public type TestRecord1 record {
    int rec1Field1 = 12;
    string rec1Field2 = "TestRecord1";
    int optionalInt?;
};

public type TestRecord2 record {
    int rec2Field1 = 12;
    string rec3Field2 = "TestRecord2";
};

type TestRecord3 record {
    int rec3Field1 = 12;
    string rec3Field2 = "TestRecord3";
};

type TestMap1 map<int>;

public type TestMap2 map<string>;

public type TestMap3 map<int>;

public class TestObject1 {
	public int field1;
	int field2;
	public function init(int field1, int field2) {
	    self.field1 = field1;
	    self.field2 = field2;
    }
}
