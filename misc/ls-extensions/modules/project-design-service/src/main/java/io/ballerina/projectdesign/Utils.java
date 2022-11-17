package io.ballerina.projectdesign;

import com.google.gson.JsonObject;
import io.ballerina.projects.Package;

import java.util.Map;

/**
 * Provide utils functions for component model building.
 *
 * @since 2201.2.2
 */
public class Utils {

    public static String getQualifiedPackageName(ComponentModel.PackageId packageId) {

        return String.format("%s/%s:%s", packageId.getOrg(),
                packageId.getName(), packageId.getVersion());
    }

    public static boolean modelAlreadyExists(Map<String, JsonObject> componentModelMap, Package currentPackage) {

        ComponentModel.PackageId packageId = new ComponentModel.PackageId(currentPackage);
        return componentModelMap.containsKey(getQualifiedPackageName(packageId));
    }
}
