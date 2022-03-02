package org.ballerinalang.semver.checker;

import com.google.gson.JsonArray;
import org.ballerinalang.semver.checker.util.FileUtils;
import org.testng.annotations.Test;

public class SemverCheckerTests {

    @Test
    public void test1() {
        JsonArray obj = new JsonArray();
        obj.add("function add(int x, int y) returns int");
        obj.add("public function  hello()");
        obj.add("public function main() ");

        FileUtils.createFile(obj , "0.1.0");

    }
}
