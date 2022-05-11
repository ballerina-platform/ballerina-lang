package org.ballerinalang.semver.checker.evaluator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.ballerina.projects.directory.BuildProject;
import org.ballerinalang.semver.checker.ProjectUtil;

import java.io.FileReader;
import java.io.IOException;

public class BaseClassTest {

    protected static void testEvaluate(String fileName) throws Exception {

        Object obj;
        JsonArray fileData = null;
        try (FileReader reader = new FileReader(fileName)) {
            obj = JsonParser.parseReader(reader);
            fileData = (JsonArray) obj;
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < fileData.size(); i++) {
            JsonObject element = (JsonObject) fileData.get(i);
            String oldCode = String.valueOf(element.get("oldCode"));
            String newCode = String.valueOf(element.get("newCode"));
            ProjectUtil project = new ProjectUtil();

            BuildProject project1 = project.createProject(oldCode);
            BuildProject project2 = project.createProject(newCode);
            // call the logic

        }

    }
}


