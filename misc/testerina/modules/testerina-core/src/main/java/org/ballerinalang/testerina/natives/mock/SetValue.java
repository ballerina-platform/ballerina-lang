/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.testerina.natives.mock;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;

/**
 * Native function ballerina.lang.mock:setValue.
 * This can be used to modify a global connector instance's arguments.
 * Behavior is reflection-like.
 *
 * @since 0.8.0
 */
@BallerinaFunction(orgName = "ballerina", packageName = "mock", functionName = "setValue", args = {
        @Argument(name = "mockableConnectorPathExpr", type = TypeKind.STRING),
        @Argument(name = "value", type = TypeKind.STRING)}, isPublic = true)
@BallerinaAnnotation(annotationName = "Description",
                     attributes = { @Attribute(name = "value",
                                               value = "Modifies global connector instance's arguments for mocking "
                                                       + "purposes") })
@BallerinaAnnotation(annotationName = "Param",
                     attributes = { @Attribute(name = "mockableConnectorPathExpr",
                                               value = "A path like syntax to identify and navigate the "
                                                       + "connector instances of a ballerina service") })
@BallerinaAnnotation(annotationName = "Param",
                     attributes = { @Attribute(name = "value",
                                               value = "Mock value to set (e.g.: endpoint URL)") })
public class SetValue extends BlockingNativeCallableUnit {

//   private static final String COULD_NOT_FIND_MATCHING_CONNECTOR =
//                                                      "Could not find a matching connector for the name ";
//
//    private static final String MSG_PREFIX = "mock:setValue: ";
//    private static final String MOCK_PATH_SYNTAX = "<ServiceName>[.]<ConnectorVariableName(s)>[.]parameterX";
//    private static final String MOCK_PATH_SYNTAX_EXAMPLE = "helloWorld.httpCon.parameter1";
//    private static final int INTEGER_OFFSET = 0;
//    private static final int FLOAT_OFFSET = 1;
//    private static final int BOOLEAN_OFFSET = 2;
//    private static final int STRING_OFFSET = 3;
//    private static final int REFTYPE_OFFSET = 4;
//
//    private static final Logger logger = LoggerFactory.getLogger(SetValue.class);

    //TODO: Improve this to support modification of local variables as well
    @Override
    public void execute(Context ctx) {
//
//        //Set the global connector instance as given by names in the path array.
//        //keep traversing the path array until the last connector (element - 1).
//        //once found, get the primitive that has the name of last element in the path array
//        //change that primitive type's value to the `value` user entered.
//        //then return
//
//        if (!TesterinaUtils.isMockEnabled()) {
//            throw new BallerinaException(
//                    MSG_PREFIX + "'--mock' parameter or the 'ballerina.mock' system property is not found. ");
//        }
//
//        //1) split the mockConnectorPath string by dots
//        //first element is the service name, in-betweens are connectors and the last element is a primitive
//        MockConnectorPath mockCnctrPath = parseMockConnectorPath(ctx);
//
//        //Locate the relevant Application, Package, and the Service
//        ServiceInfo serviceInfo = getMatchingService(mockCnctrPath.serviceName);
//
//        if (mockCnctrPath.connectorNames.size() < 1) {
//            throw new BallerinaException(
//                    "Connectors entered for the service " + mockCnctrPath.serviceName + " is empty." + ". Syntax: "
//                            + MOCK_PATH_SYNTAX + ". Example: " + MOCK_PATH_SYNTAX_EXAMPLE);
//        }
//
//        BConnector connector = getConnectorValue(ctx, serviceInfo, mockCnctrPath);
//
//        BType[] fieldTypes = connector.getFieldTypes();
//        int[] varTypeIndex = new int[5];
//
//        for (int i = 0; i <= mockCnctrPath.indexOfMockField; i++) {
//            switch (fieldTypes[i].getName()) {
//                case TypeConstants.INT_TNAME:
//                    varTypeIndex[INTEGER_OFFSET]++;
//                    break;
//                case TypeConstants.FLOAT_TNAME:
//                    varTypeIndex[FLOAT_OFFSET]++;
//                    break;
//                case TypeConstants.BOOLEAN_TNAME:
//                    varTypeIndex[BOOLEAN_OFFSET]++;
//                    break;
//                case TypeConstants.STRING_TNAME:
//                    varTypeIndex[STRING_OFFSET]++;
//                    break;
//                default:
//                    varTypeIndex[REFTYPE_OFFSET]++;
//                    break;
//            }
//        }
//
//        String fieldType = fieldTypes[mockCnctrPath.indexOfMockField].getName();
//        setVarValue(connector, mockCnctrPath.terminalVarName, fieldType, mockCnctrPath.mockValue, varTypeIndex);

        ctx.setReturnValues();
    }

//    private ServiceInfo getMatchingService(String serviceName) {
//        Optional<ServiceInfo> matchingService = Optional.empty();
//        for (ProgramFile programFile : TesterinaRegistry.getInstance().getProgramFiles()) {
//            // 1) First, we get the Service for the given serviceName from the original ProgramFile
//            matchingService = Arrays.stream(programFile.getServicePackageNameList())
//                    .map(sName -> programFile.getPackageInfo(sName).getServiceInfoList())
//                    .flatMap(Arrays::stream)
//                    .filter(serviceInfo -> serviceInfo.getName().equals(serviceName))
//                    .findAny();
//        }
//
//        // fail further processing if we can't find the application/service
//        if (!matchingService.isPresent()) {
//            // Added for user convenience. Since we are stopping further progression of the program,
//            // perf overhead is ignored.
//            Set<String> servicesSet = TesterinaRegistry.getInstance().getProgramFiles().stream()
//                    .map(ProgramFile::getServicePackageNameList).flatMap(Arrays::stream)
//                    .collect(Collectors.toSet());
//
//            throw new BallerinaException(MSG_PREFIX + "No matching service for the name '" + serviceName + "' found. "
//                    + "Did you mean to include one of these services? " + servicesSet);
//        }
//
//        return matchingService.get();
//    }
//
//    private MockConnectorPath parseMockConnectorPath(Context ctx) {
//        String mockCntrPathString = ctx.getStringArgument(0);
//        String mockValue = ctx.getStringArgument(1);
//        String[] mockCntrPathArr = mockCntrPathString.split("\\.");
//        if (mockCntrPathArr.length < 2) {
//            throw new BallerinaException(
//                    "Error in parsing " + mockCntrPathString + ". Syntax: " + MOCK_PATH_SYNTAX + ". Example: "
//                            + MOCK_PATH_SYNTAX_EXAMPLE);
//        }
//
//        String paramName = mockCntrPathArr[mockCntrPathArr.length - 1];
//
//        //in case of multi-argument connector, we need a way to find the exact argument to update
//        //for ballerina connectors, it can be derived via ParameterDefStmts of the connector
//        //but for native connectors, it is not possible. Hence, user need to specify the index.
//        //ex. helloService.myConnector.parameter2 ==> update the 2nd parameter of the connector
//        int indexOfMockField = 0;
//        Pattern pattern = Pattern.compile("^([0-9]*).+");
//        Matcher matcher = pattern.matcher(new StringBuilder(paramName).reverse());
//        if (matcher.matches()) {
//            String group = new StringBuilder(matcher.group(1)).reverse().toString();
//            if (!group.isEmpty()) {
//                indexOfMockField = Integer.parseInt(group);
//                indexOfMockField--; //user inputs for the parameter indexes begin from 1, but for us it's 0
//            }
//        }
//
//        LinkedList<String> connectorNamesList = new LinkedList<>(
//                Arrays.asList(Arrays.copyOfRange(mockCntrPathArr, 1, mockCntrPathArr.length - 1)));
//        return new MockConnectorPath(mockCntrPathString, mockCntrPathArr[0], connectorNamesList, paramName, mockValue,
//                indexOfMockField);
//    }
//
//    /**
//     * Get the value of the connector whose variable is to be mocked.
//     *
//     * @param ctx Current context
//     * @param serviceInfo Service Info of the parent service of the connector
//     * @param mockCnctrPath A path like syntax to identify and navigate the connector instances of a ballerina service
//     * @return Value of the connector whose variable is to be mocked
//     */
//    private BConnector getConnectorValue(Context ctx, ServiceInfo serviceInfo, MockConnectorPath mockCnctrPath) {
//        // find the connector
//        String firstConnectorName = mockCnctrPath.connectorNames.pop();
//        LocalVariableAttributeInfo localVars =
//            (LocalVariableAttributeInfo) serviceInfo.getAttributeInfo(AttributeInfo.LOCAL_VARIABLES_ATTRIBUTE);
//        LocalVariableInfo firstConVarInfo = localVars.getLocalVariables().stream()
//                .filter(varInfo -> varInfo.getVariableName().equals(firstConnectorName))
//                .findAny()
//                .orElse(null);
//        if (firstConVarInfo == null) {
//            throw new BallerinaException(COULD_NOT_FIND_MATCHING_CONNECTOR + firstConnectorName);
//        }
//
//        StructureType globalMemBlock = ctx.getProgramFile().getGlobalMemoryBlock();
//        BConnector connector = (BConnector) globalMemBlock.getRefField(firstConVarInfo.getVariableIndex());
//
//        return getLastConnector(ctx.programFile, connector, firstConVarInfo, mockCnctrPath.connectorNames);
//    }
//
//    /**
//     * Get the value of the last connector in the sequence of connectors to be mocked.
//     *
//     * @param programFile Program file
//     * @param connector Current connector value
//     * @param conVarInfo Variable info of the current connector
//     * @param connectorNames remaining connector names to be mocked
//     * @return Value of the last connector in the sequence
//     */
//    private BConnector getLastConnector(ProgramFile programFile, BConnector connector, LocalVariableInfo conVarInfo,
//            Queue<String> connectorNames) {
//        String connectorNameToLookFor = connectorNames.poll();
//        if (connectorNameToLookFor == null) {
//            return connector;
//        }
//
//        String connectorPkgPath = conVarInfo.getVariableType().getPackagePath();
//        String connectorName = conVarInfo.getVariableType().getName();
//
//        ConnectorInfo connectorInfo = programFile.getPackageInfo(connectorPkgPath).getConnectorInfo(connectorName);
//        LocalVariableAttributeInfo localVars = (LocalVariableAttributeInfo) connectorInfo
//                .getAttributeInfo(AttributeInfo.LOCAL_VARIABLES_ATTRIBUTE);
//        LocalVariableInfo innerConVarInfo = localVars.getLocalVariables().stream()
//                .filter(varInfo -> varInfo.getVariableName().equals(connectorNameToLookFor))
//                .findAny()
//                .orElse(null);
//        if (innerConVarInfo == null) {
//            throw new BallerinaException(COULD_NOT_FIND_MATCHING_CONNECTOR + connectorNameToLookFor);
//        }
//
//        connector = (BConnector) connector.getRefField(innerConVarInfo.getVariableIndex());
//
//        return getLastConnector(programFile, connector, innerConVarInfo, connectorNames);
//    }
//
//    /**
//     * Set the value of the local variable of the connector
//     *
//     * @param connector Connector
//     * @param varName Local variable name
//     * @param varType Local variable type
//     * @param value Value of the local variable
//     * @param fieldIndexes Indexes of the variables
//     */
//    private void setVarValue(BConnector connector, String varName, String varType, String value, int[] fieldIndexes) {
//        try {
//            switch (varType) {
//                case TypeConstants.INT_TNAME:
//                    connector.setIntField(fieldIndexes[INTEGER_OFFSET] - 1, Integer.parseInt(value));
//                    break;
//                case TypeConstants.FLOAT_TNAME:
//                    connector.setFloatField(fieldIndexes[FLOAT_OFFSET] - 1, Double.valueOf(value));
//                    break;
//                case TypeConstants.BOOLEAN_TNAME:
//                    connector.setBooleanField(fieldIndexes[BOOLEAN_OFFSET] - 1, Integer.parseInt(value));
//                    break;
//                case TypeConstants.STRING_TNAME:
//                    connector.setStringField(fieldIndexes[STRING_OFFSET] - 1, value);
//                    break;
//                default:
//                    throw new BallerinaException("unsupported type '" + varType + "'. must be one of: string, int, " +
//                            "float, boolean.");
//            }
//        } catch (Throwable t) {
//            throw new BallerinaException("Error while updating the field " + varName + " to " + value + ": "
//                    + t.getMessage());
//        }
//    }
//
//    /**
//     * This is the parsed model of the user's mockConnectorPath argument.
//     */
//    protected static class MockConnectorPath {
//        String originalString;
//        String serviceName;
//        LinkedList<String> connectorNames;
//        String terminalVarName;
//        String mockValue;
//        int indexOfMockField = 0;
//
//        MockConnectorPath(String mockCntrPathString, String serviceName, LinkedList<String> connectorNames,
//                String terminalVarName, String mockValue, int indexOfMockField) {
//            this.originalString = mockCntrPathString;
//            this.serviceName = serviceName;
//            this.connectorNames = connectorNames;
//            this.terminalVarName = terminalVarName;
//            this.mockValue = mockValue;
//            this.indexOfMockField = indexOfMockField;
//        }
//
//        @Override
//        public String toString() {
//            return originalString;
//        }
//    }

}
