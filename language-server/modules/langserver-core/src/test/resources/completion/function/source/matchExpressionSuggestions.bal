type testRecord record {
    int recordInt = 12;
    string recordString = "Hello Record";
    boolean recordBoolean = false;
};


function testMatchExpression() {
    string | boolean | float | int | testRecord | () testUnionVal = "";

    boolean testBoolean = testUnionVal but {
        
    };

    string testString = testUnionVal but {
        
    };

    float testFloat = testUnionVal but {
        
    };

    int testInt = testUnionVal but {
        
    };

    testRecord testRec = testUnionVal but {
        
    };
}
