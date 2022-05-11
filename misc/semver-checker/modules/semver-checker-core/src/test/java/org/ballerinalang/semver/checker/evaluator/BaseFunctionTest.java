package org.ballerinalang.semver.checker.evaluator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.ballerina.projects.Package;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.semver.checker.comparator.PackageComparator;
import io.ballerina.semver.checker.diff.PackageDiff;
import org.ballerinalang.semver.checker.ProjectUtil;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;

public class BaseFunctionTest {

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
            BuildProject oldProject = project.createProject(oldCode);
            BuildProject currentProject2 = project.createProject(newCode);
            Package oldPackage = oldProject.currentPackage();
            Package currentPackage = currentProject2.currentPackage();
            PackageComparator packageComparator = new PackageComparator(currentPackage,oldPackage);
            Optional<PackageDiff> packageDiff = packageComparator.computeDiff();
            packageDiff.get().getModuleDiffs();
            packageDiff.get().getAsString();
            System.out.println(packageComparator.computeDiff());
        }
    }
}






