package org.ballerinalang.containers.docker.cmd;

import org.ballerinalang.containers.Constants;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Test DockerCmd functionality.
 */
public class DockerCmdTest {

    @Test
    public void testImageNameExtractionNoVersion()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Class<DockerCmd> cmdClass = DockerCmd.class;
        Method getImageNameDetailsMethod = cmdClass.getDeclaredMethod("getImageNameDetails",
                String.class, String.class);
        getImageNameDetailsMethod.setAccessible(true);
        String imageName = "someImageName";
        String packageName = "packageName";
        String[] imageNameParts = (String[]) getImageNameDetailsMethod.invoke(cmdClass, imageName, packageName);
        Assert.assertEquals(imageNameParts.length, 2, "Invalid number of values returned");
        Assert.assertEquals(imageNameParts[0], imageName.toLowerCase(Locale.getDefault()),
                "Invalid image name returned");
        Assert.assertEquals(imageNameParts[1], Constants.IMAGE_VERSION_LATEST,
                "Invalid image version returned");
    }

    @Test
    public void testImageNameExtractionWithVersion()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Class<DockerCmd> cmdClass = DockerCmd.class;
        Method getImageNameDetailsMethod = cmdClass.getDeclaredMethod("getImageNameDetails",
                String.class, String.class);
        getImageNameDetailsMethod.setAccessible(true);
        String imageName = "someImageName";
        String imageVersion = "0.0.1";
        String packageName = "packageName";
        String[] imageNameParts = (String[]) getImageNameDetailsMethod.invoke(cmdClass,
                imageName + ":" + imageVersion, packageName);
        Assert.assertEquals(imageNameParts.length, 2, "Invalid number of values returned");
        Assert.assertEquals(imageNameParts[0], imageName.toLowerCase(Locale.getDefault()),
                "Invalid image name returned");
        Assert.assertEquals(imageNameParts[1], imageVersion,
                "Invalid image version returned");
    }

    @Test
    public void testImageNameExtractionNoGivenName()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Class<DockerCmd> cmdClass = DockerCmd.class;
        Method getImageNameDetailsMethod = cmdClass.getDeclaredMethod("getImageNameDetails",
                String.class, String.class);
        getImageNameDetailsMethod.setAccessible(true);
        String packageName = "packageName";
        String[] imageNameParts = (String[]) getImageNameDetailsMethod.invoke(cmdClass, null, packageName);
        Assert.assertEquals(imageNameParts.length, 2, "Invalid number of values returned");
        Assert.assertEquals(imageNameParts[0], packageName.toLowerCase(Locale.getDefault()),
                "Invalid image name returned");
        Assert.assertEquals(imageNameParts[1], Constants.IMAGE_VERSION_LATEST,
                "Invalid image version returned");
    }
}
