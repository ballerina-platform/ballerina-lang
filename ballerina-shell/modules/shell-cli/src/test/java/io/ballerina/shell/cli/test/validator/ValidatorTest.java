package io.ballerina.shell.cli.test.validator;
import io.ballerina.shell.cli.jline.validator.InputValidator;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ValidatorTest {

    @Test
    public void testValidator() {
        InputValidator inputValidator = new InputValidator();
        Assert.assertFalse(inputValidator.isIncomplete("int i = 12"));
        Assert.assertFalse(inputValidator.isIncomplete("int i = 12;"));
        Assert.assertFalse(inputValidator.isIncomplete("int[] x = [1,2];"));
        Assert.assertFalse(inputValidator.isIncomplete("foreach var emp in top3 {\n" +
                "        io:println(emp);\n" +
                "}"));
        Assert.assertFalse(inputValidator.isIncomplete("Employee[] employees = [\n" +
                "        {firstName: \"Jones\", lastName: \"Welsh\", salary: 1000.00},\n" +
                "    ];"));
        Assert.assertFalse(inputValidator.isIncomplete("Employee[] top3 = from var e in employees\n" +
                "                      order by e.salary descending\n" +
                "\n" +
                "                      limit 3\n" +
                "\n" +
                "                      select e;"));

        Assert.assertTrue(inputValidator.isIncomplete("x + "));
        Assert.assertTrue(inputValidator.isIncomplete("[1,2,3"));
        Assert.assertTrue(inputValidator.isIncomplete("int[] x = [1,2,3,"));
        Assert.assertTrue(inputValidator.isIncomplete("function parse(string s) returns int|error {"));
        Assert.assertTrue(inputValidator.isIncomplete("function parse(string s)"));
        Assert.assertTrue(inputValidator.isIncomplete("foreach var emp in top3"));
        Assert.assertTrue(inputValidator.isIncomplete("type Coord record"));
        Assert.assertTrue(inputValidator.isIncomplete("type Coord record {\n" + "int x;"));
        Assert.assertTrue(inputValidator.isIncomplete("Employee[] employees = [\n" +
                "        {firstName: \"Jones\", lastName: \"Welsh\", salary: 1000.00},"));
        Assert.assertTrue(inputValidator.isIncomplete("Employee[] top3 = from var e in employees\n" +
                "                      order by e.salary descending"));
        Assert.assertTrue(inputValidator.isIncomplete(
                "Employee[] top3 = from var e in employees\n" +
                "                      order by e.salary descending\n" +
                "\n" +
                "                      limit 3\n" +
                "\n" +
                "                      select"));
        Assert.assertTrue(inputValidator.isIncomplete("int[] evenNums = from var i in nums\n" +
                "                     where i % 2 == 0"));
    }
}