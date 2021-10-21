package org.ballerinalang.diagramutil;

import org.ballerinalang.diagramutil.connector.models.BalaFile;
import org.ballerinalang.diagramutil.connector.models.Error;
import org.ballerinalang.diagramutil.connector.models.connector.Connector;
import org.ballerinalang.diagramutil.connector.models.connector.Function;
import org.ballerinalang.diagramutil.connector.models.connector.Type;
import org.ballerinalang.diagramutil.connector.models.connector.TypeInfo;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Connector models test case.
 */
public class TestConnectorModels {
    private final String connectorName = "TestConnector";
    private final String orgName = "testOrg";
    private final String moduleName = "testModule";
    private final String packageName = "testPackage";
    private final String version = "0.1.0";
    private final String documentation = "test documentation";
    private final String recordType = "record";
    private final String initFunc = "init";
    private final String path = "/test/org/testConnector";
    private final String errorMsg = "test error message";
    private TypeInfo typeInfo;
    private Type type;
    private Function function;
    private Connector connector;
    private BalaFile balaFile;
    private Error error;

    @BeforeClass
    public void initConnectorModels() {
        typeInfo = new TypeInfo(connectorName, orgName, moduleName, packageName, version);

        type = new Type();
        type.typeName = recordType;
        type.optional = false;
        type.typeInfo = this.typeInfo;

        List<Type> parameters = Arrays.asList(type);
        function = new Function(initFunc, parameters, Optional.of(type), null, false, documentation);

        List<Function> functions = Arrays.asList(function);
        connector = new Connector(orgName, moduleName, packageName, version, connectorName,
                documentation, null, functions);

        balaFile = new BalaFile();
        balaFile.setPath(path);

        error = new Error(errorMsg);
    }


    @Test(description = "Test ballerina typeInfo object")
    public void getTypeInfo() {
        Assert.assertEquals(typeInfo.orgName, orgName);
        Assert.assertEquals(typeInfo.moduleName, moduleName);
        Assert.assertEquals(typeInfo.packageName, packageName);
        Assert.assertEquals(typeInfo.version, version);
    }

    @Test(description = "Test ballerina type object")
    public void getType() {
        Assert.assertEquals(type.typeName, recordType);
        Assert.assertEquals(type.optional, false);
        Assert.assertEquals(type.typeInfo.packageName, packageName);
    }

    @Test(description = "Test ballerina function object")
    public void getFunction() {
        Assert.assertEquals(function.name, initFunc);
        Assert.assertEquals(function.parameters.size(), 1);
        Assert.assertEquals(function.parameters.get(0).typeName, recordType);
        Assert.assertEquals(function.returnType.optional, false);
        Assert.assertEquals(function.documentation, documentation);
    }

    @Test(description = "Test ballerina connector object")
    public void getConnector() {
        Assert.assertEquals(connector.moduleName, moduleName);
        Assert.assertEquals(connector.packageInfo.getOrganization(), orgName);
        Assert.assertEquals(connector.packageInfo.getName(), packageName);
        Assert.assertEquals(connector.functions.size(), 1);
        Assert.assertEquals(connector.functions.get(0).name, initFunc);
    }

    @Test(description = "Test bala file object")
    public void getBalaFile() {
        Assert.assertEquals(balaFile.getPath(), path);
    }

    @Test(description = "Test connector generator error object")
    public void getError() {
        Assert.assertEquals(error.getMessage(), errorMsg);
    }
}
