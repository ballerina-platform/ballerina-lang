package org.ballerinalang.test.query;

import org.ballerinalang.model.values.*;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AdditionalScenariosTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/additional-scenarios.bal");
    }

    @Test(description = "Test more than two from clauses")
    public void testMultipleFromAndSelectClausesWithRecordVariable() {
        BValue[] returnValues = BRunUtil.invoke(result, "testMultipleFromAndSelectClausesWithRecordVariable");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 8, "Expected events are not received");

        BMap<String, BValue> person1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> person2 = (BMap<String, BValue>) returnValues[1];
        BMap<String, BValue> person3 = (BMap<String, BValue>) returnValues[2];
        BMap<String, BValue> person4 = (BMap<String, BValue>) returnValues[3];
        BMap<String, BValue> person5 = (BMap<String, BValue>) returnValues[4];
        BMap<String, BValue> person6 = (BMap<String, BValue>) returnValues[5];
        BMap<String, BValue> person7 = (BMap<String, BValue>) returnValues[6];
        BMap<String, BValue> person8 = (BMap<String, BValue>) returnValues[7];

        BMap<String, BValue> person1Address = (BMap<String, BValue>) person1.get("address");
        BMap<String, BValue> person2Address = (BMap<String, BValue>) person2.get("address");
        BMap<String, BValue> person3Address = (BMap<String, BValue>) person3.get("address");
        BMap<String, BValue> person4Address = (BMap<String, BValue>) person4.get("address");
        BMap<String, BValue> person5Address = (BMap<String, BValue>) person5.get("address");
        BMap<String, BValue> person6Address = (BMap<String, BValue>) person6.get("address");
        BMap<String, BValue> person7Address = (BMap<String, BValue>) person7.get("address");
        BMap<String, BValue> person8Address = (BMap<String, BValue>) person8.get("address");


        Assert.assertEquals(person1.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(person1.get("lastName").stringValue(), "George");
        Assert.assertEquals(person1.get("deptAccess").stringValue(), "HR");
        Assert.assertEquals(person1Address.get("city").stringValue(), "New York");
        Assert.assertEquals(person1Address.get("country").stringValue(), "USA");

        Assert.assertEquals(person2.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(person2.get("lastName").stringValue(), "George");
        Assert.assertEquals(person2.get("deptAccess").stringValue(), "HR");
        Assert.assertEquals(person2Address.get("city").stringValue(), "Springfield");
        Assert.assertEquals(person2Address.get("country").stringValue(), "USA");

        Assert.assertEquals(person3.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(person3.get("lastName").stringValue(), "George");
        Assert.assertEquals(person3.get("deptAccess").stringValue(), "Operations");
        Assert.assertEquals(person3Address.get("city").stringValue(), "New York");
        Assert.assertEquals(person3Address.get("country").stringValue(), "USA");

        Assert.assertEquals(person4.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(person4.get("lastName").stringValue(), "George");
        Assert.assertEquals(person4.get("deptAccess").stringValue(), "Operations");
        Assert.assertEquals(person4Address.get("city").stringValue(), "Springfield");
        Assert.assertEquals(person4Address.get("country").stringValue(), "USA");

        Assert.assertEquals(person5.get("firstName").stringValue(), "Ranjan");
        Assert.assertEquals(person5.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(person5.get("deptAccess").stringValue(), "HR");
        Assert.assertEquals(person5Address.get("city").stringValue(), "New York");
        Assert.assertEquals(person5Address.get("country").stringValue(), "USA");

        Assert.assertEquals(person6.get("firstName").stringValue(), "Ranjan");
        Assert.assertEquals(person6.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(person6.get("deptAccess").stringValue(), "HR");
        Assert.assertEquals(person6Address.get("city").stringValue(), "Springfield");
        Assert.assertEquals(person6Address.get("country").stringValue(), "USA");

        Assert.assertEquals(person7.get("firstName").stringValue(), "Ranjan");
        Assert.assertEquals(person7.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(person7.get("deptAccess").stringValue(), "Operations");
        Assert.assertEquals(person7Address.get("city").stringValue(), "New York");
        Assert.assertEquals(person7Address.get("country").stringValue(), "USA");

        Assert.assertEquals(person8.get("firstName").stringValue(), "Ranjan");
        Assert.assertEquals(person8.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(person8.get("deptAccess").stringValue(), "Operations");
        Assert.assertEquals(person8Address.get("city").stringValue(), "Springfield");
        Assert.assertEquals(person8Address.get("country").stringValue(), "USA");


    }

    @Test(description = "Test logical operands with where")
    public void testLogicalOperandsWithWhere() {
        BValue[] returnValues = BRunUtil.invoke(result, "testLogicalOperandsWithWhere");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");

        BMap<String, BValue> person1 = (BMap<String, BValue>) returnValues[0];

        Assert.assertEquals(person1.get("firstName").stringValue(), "Ranjan");
        Assert.assertEquals(person1.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(((BFloat) person1.get("score")).floatValue(), 90.6);
    }

    @Test(description = "Test query expressions with open records")
    public void testQueryExprWithOpenRecords() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithOpenRecords");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 4, "Expected events are not received");

        BMap section1 = (BMap) returnValues[0];
        BMap section2 = (BMap) returnValues[1];
        BMap section3 = (BMap) returnValues[2];
        BMap section4 = (BMap) returnValues[3];

        Assert.assertTrue(section1.get("grades") instanceof BMap);
        Assert.assertEquals(section1.get("grades").stringValue(),"{physics:30, chemistry:50, maths:60}");
        Assert.assertEquals(section1.stringValue(),"{name:\"Maths\", grades:{physics:30, chemistry:50, maths:60}, noOfStudents:100}");

        Assert.assertTrue(section2.get("grades") instanceof BMap);
        Assert.assertEquals(section2.get("grades").stringValue(),"{physics:50, chemistry:60, bio:70}");
        Assert.assertEquals(section2.stringValue(),"{name:\"Maths\", grades:{physics:50, chemistry:60, bio:70}, noOfStudents:100}");

        Assert.assertTrue(section3.get("grades") instanceof BMap);
        Assert.assertEquals(section3.get("grades").stringValue(),"{physics:30, chemistry:50, maths:60}");
        Assert.assertEquals(section3.stringValue(),"{name:\"Bio\", grades:{physics:30, chemistry:50, maths:60}, noOfStudents:100}");

        Assert.assertTrue(section4.get("grades") instanceof BMap);
        Assert.assertEquals(section4.get("grades").stringValue(),"{physics:50, chemistry:60, bio:70}");
        Assert.assertEquals(section4.stringValue(),"{name:\"Bio\", grades:{physics:50, chemistry:60, bio:70}, noOfStudents:100}");

    }

    @Test(description = "Test anonymous record type, record type referencing, optional field, changed order of the fields")
    public void testOthersAssociatedWithRecordTypes() {
        BValue[] returnValues = BRunUtil.invoke(result, "testOthersAssociatedWithRecordTypes");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 2, "Expected events are not received");

        BMap teacher1 = (BMap) returnValues[0];
        BMap teacher2 = (BMap) returnValues[1];

        Assert.assertTrue(teacher1.get("classStudents") instanceof BValueArray);
        Assert.assertTrue(teacher1.get("experience") instanceof BMap);
        Assert.assertEquals(teacher1.get("classStudents").stringValue(),"[{firstName:\"Alex\", lastName:\"George\", score:82.5}, {firstName:\"Ranjan\", lastName:\"Fonseka\", score:90.6}]");
        Assert.assertEquals(teacher1.get("experience").stringValue(),"{duration:10, qualitifications:\"B.Sc.\"}");
        Assert.assertEquals(teacher1.stringValue(),"{classStudents:[{firstName:\"Alex\", lastName:\"George\", score:82.5}, {firstName:\"Ranjan\", lastName:\"Fonseka\", score:90.6}], experience:{duration:10, qualitifications:\"B.Sc.\"}, firstName:\"Alex\", lastName:\"George\", deptAccess:\"XYZ\", address:{city:\"NY\", country:\"America\"}}");


        Assert.assertTrue(teacher2.get("classStudents") instanceof BValueArray);
        Assert.assertTrue(teacher2.get("experience") instanceof BMap);
        Assert.assertEquals(teacher2.get("classStudents").stringValue(),"[{firstName:\"Alex\", lastName:\"George\", score:82.5}, {firstName:\"Ranjan\", lastName:\"Fonseka\", score:90.6}]");
        Assert.assertEquals(teacher2.get("experience").stringValue(),"{duration:10, qualitifications:\"B.Sc.\"}");
        Assert.assertEquals(teacher2.stringValue(),"{classStudents:[{firstName:\"Alex\", lastName:\"George\", score:82.5}, {firstName:\"Ranjan\", lastName:\"Fonseka\", score:90.6}], experience:{duration:10, qualitifications:\"B.Sc.\"}, firstName:\"Ranjan\", lastName:\"Fonseka\", deptAccess:\"XYZ\", address:{city:\"NY\", country:\"America\"}}");

    }

    @Test(description = "Test query expressions with tuple typed binding")
    public void testQueryExprTupleTypedBinding1() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprTupleTypedBinding1");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");

        Assert.assertEquals(((BValueArray) returnValues[0]).getString(0), "A");
        Assert.assertEquals(((BValueArray) returnValues[0]).getString(1), "B");
        Assert.assertEquals(((BValueArray) returnValues[0]).getString(2), "C");

    }

    @Test(description = "Test query expressions with tuple typed binding in let")
    public void testQueryExprTupleTypedBinding2() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprTupleTypedBinding2");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");

        Assert.assertEquals(((BValueArray)returnValues[0]).getInt(0), 3);

    }

    @Test(description = "Test query expression with record typed binding")
    public void testQueryExprRecordTypedBinding() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprRecordTypedBinding");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 4, "Expected events are not received");

        BMap person1 = (BMap) returnValues[0];
        BMap person2 = (BMap) returnValues[1];
        BMap person3 = (BMap) returnValues[2];
        BMap person4 = (BMap) returnValues[3];

        Assert.assertTrue(person1.get("address") instanceof BMap);
        Assert.assertEquals(person1.stringValue(),"{firstName:\"Alex\", lastName:\"George\", deptAccess:\"HR\", address:{city:\"NY\", country:\"America\"}}");
        Assert.assertEquals(person2.stringValue(),"{firstName:\"Alex\", lastName:\"George\", deptAccess:\"Operations\", address:{city:\"NY\", country:\"America\"}}");
        Assert.assertEquals(person3.stringValue(),"{firstName:\"Ranjan\", lastName:\"Fonseka\", deptAccess:\"HR\", address:{city:\"NY\", country:\"America\"}}");
        Assert.assertEquals(person4.stringValue(),"{firstName:\"Ranjan\", lastName:\"Fonseka\", deptAccess:\"Operations\", address:{city:\"NY\", country:\"America\"}}");
    }

    @Test(description = "Test query expression with type conversion in select clause")
    public void testQueryExprWithTypeConversion() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithTypeConversion");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 2, "Expected events are not received");

        BMap person1 = (BMap) returnValues[0];
        BMap person2 = (BMap) returnValues[1];

        Assert.assertEquals(person1.stringValue(),"{firstName:\"Alex\", lastName:\"George\", deptAccess:\"XYZ\", address:{city:\"New York\", country:\"America\"}}");
        Assert.assertEquals(person2.stringValue(),"{firstName:\"Ranjan\", lastName:\"Fonseka\", deptAccess:\"XYZ\", address:{city:\"New York\", country:\"America\"}}");

    }

    @Test(description = "Test streams with map and filter")
    public void testQueryExprWithStreamMapAndFilter() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithStreamMapAndFilter");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");

        BMap subscription = (BMap) returnValues[0];

        Assert.assertEquals(subscription.stringValue(),"{firstName:\"Ranjan\", lastName:\"Fonseka\", score:90.6, degree:\"Bachelor of Medicine\"}");

    }

}


