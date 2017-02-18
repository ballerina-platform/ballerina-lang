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
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.BallerinaConnectorDef;
import org.ballerinalang.model.Connector;
import org.ballerinalang.model.ParameterDef;
import org.ballerinalang.model.Service;
import org.ballerinalang.model.expressions.BasicLiteral;
import org.ballerinalang.model.expressions.ConnectorInitExpr;
import org.ballerinalang.model.expressions.Expression;
import org.ballerinalang.model.statements.VariableDefStmt;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.testerina.core.TesterinaRegistry;
import org.ballerinalang.testerina.core.TesterinaUtils;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.security.AccessController.doPrivileged;

/**
 * Native function ballerina.lang.mock:setValue.
 * This can be used to modify a global connector instance's arguments.
 * Behavior is reflection-like.
 *
 * @since 0.8.0
 */
@BallerinaFunction(packageName = "ballerina.lang.mock", functionName = "setValue", args = {
        @Argument(name = "mockConnectorPath", type = TypeEnum.STRING),
        @Argument(name = "value", type = TypeEnum.STRING) }, isPublic = true)
public class SetValue extends AbstractNativeFunction {

    public static final String FIELD_NAME_VALUE = "value";
    private static final String COULD_NOT_FIND_MATCHING_CONNECTOR = "Could not find a matching connector for the name ";

    private static final String MSG_PREFIX = "mock:setValue: ";
    private static final String MOCK_PATH_SYNTAX = "<ServiceName>[.]<ConnectorVariableName(s)>[.]parameterX";
    private static final String MOCK_PATH_SYNTAX_EXAMPLE = "helloWorld.httpCon.parameter1";

    private static final Logger logger = LoggerFactory.getLogger(SetValue.class);

    @Override
    public BValue[] execute(Context ctx) {

        //Set the global connector instance as given by names in the path array.
        //keep traversing the path array until the last connector (element - 1).
        //once found, get the primitive that has the name of last element in the path array
        //change that primitive type's value to the `value` user entered.
        //then return

        if (!TesterinaUtils.isMockEnabled()) {
            throw new BallerinaException(
                    MSG_PREFIX + "'--mock' parameter or the 'ballerina.mock' system property is not found. ");
        }

        //1) split the mockConnectorPath string by dots
        //first element is the service name, in-betweens are connectors and the last element is a primitive
        MockConnectorPath mockCnctrPath = parseMockConnectorPath(ctx);

        //Locate the relevant Application, Package, and the Service
        Service service = getMatchingService(mockCnctrPath.serviceName);

        //traverse through the connectors and get the last connector instance
        VariableDefStmt[] variableDefStmts = service.getVariableDefStmts();
        if (mockCnctrPath.connectorNames.size() < 1) {
            throw new BallerinaException(
                    "Connectors entered for the service " + mockCnctrPath.serviceName + " is empty." + ". Syntax: "
                            + MOCK_PATH_SYNTAX + ". Example: " + MOCK_PATH_SYNTAX_EXAMPLE);
        }
        String firstConnectorName = mockCnctrPath.connectorNames.pop();
        VariableDefStmt variableDefStmt = Arrays.stream(variableDefStmts)
                .filter(connector -> connector.getVariableDef().getName().equals(firstConnectorName)).findAny()
                .orElse(null);
        if (variableDefStmt == null) {
            throw new BallerinaException(COULD_NOT_FIND_MATCHING_CONNECTOR + firstConnectorName);
        }
        variableDefStmt = getLastConnector(variableDefStmt, mockCnctrPath.connectorNames);
        if (!(variableDefStmt.getRExpr() instanceof ConnectorInitExpr)) {
            throw new BallerinaException(
                    MSG_PREFIX + "The mock connector path argument contains values other than connector definitions "
                            + mockCnctrPath.originalString);
        }

        //We are updating an argument in the last connector expression
        Expression[] argExprs = ((ConnectorInitExpr) variableDefStmt.getRExpr()).getArgExprs();

        boolean isMockValueSet = false;
        if (variableDefStmt.getVariableDef().getType() instanceof BallerinaConnectorDef) {
            Connector balConDef = (Connector) variableDefStmt.getVariableDef().getType();
            for (int i = 0; i < balConDef.getParameterDefs().length; i++) { //find in connector parameters
                ParameterDef[] paraDef = balConDef.getParameterDefs();
                //the # of parameters and # of arguments must match, and is ordered by the array index.
                if (paraDef[i].getName().equals(mockCnctrPath.terminalVarName)) {
                    setProperty(argExprs[i], mockCnctrPath.terminalVarName, mockCnctrPath.mockValue);
                    isMockValueSet = true;
                    break;
                }
            }
        }
        //fall back to index based argument update
        if (!isMockValueSet) {
            validateArgsExprsArray(mockCnctrPath.indexOfMockField, argExprs, variableDefStmt);

            setProperty(argExprs[mockCnctrPath.indexOfMockField], mockCnctrPath.terminalVarName,
                    mockCnctrPath.mockValue);
        }

        //        if (variableDefStmt.getVariableDef().getType() instanceof AbstractNativeConnector) {
        //            BLangProgram bLangProgram = service.getBLangProgram();
        //            RuntimeEnvironment reinitRuntimeEnvironment = RuntimeEnvironment.get(bLangProgram);
        //            serviceMetadata.application.setRuntimeEnv(reinitRuntimeEnvironment);
        //        }

        return VOID_RETURN;
    }

    private Service getMatchingService(String serviceName) {
        Optional<Service> matchingService = Optional.empty();
        for (BLangProgram bLangProgram : TesterinaRegistry.getInstance().getBLangPrograms()) {
            // 1) First, we get the Service for the given serviceName from the original BLangProgram
            matchingService = Arrays.stream(bLangProgram.getServicePackages()).map(BLangPackage::getServices)
                    .flatMap(Arrays::stream).filter(s -> s.getName().equals(serviceName)).findAny();
        }

        // fail further processing if we can't find the application/service
        if (!matchingService.isPresent()) {
            // Added for user convenience. Since we are stopping further progression of the program,
            // perf overhead is ignored.
            Set<String> servicesSet = TesterinaRegistry.getInstance().getBLangPrograms().stream()
                    .map(BLangProgram::getServicePackages).flatMap(Arrays::stream).map(BLangPackage::getServices)
                    .flatMap(Arrays::stream).map(Service::getName).collect(Collectors.toSet());

            throw new BallerinaException(MSG_PREFIX + "No matching service for the name '" + serviceName + "' found. "
                    + "Did you mean to include one of these services? " + servicesSet);
        }

        return matchingService.get();
    }

/*
    */

    /**
     * Get rid of this method once {@code ApplicationRegistry.getAllApplications}
     * method is available.
     *//*

    private Collection<Application> getAllApplications() {
        try {
            Field field = ApplicationRegistry.class.getDeclaredField("applications");
            doPrivileged((PrivilegedAction<Object>) () -> {
                field.setAccessible(true);
                return null;
            });

            return ((Map<String, Application>) field.get(ApplicationRegistry.getInstance())).values();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new BallerinaException("Error while getting the list of all applications - " + e.getMessage());
        }
    }

*/
    private void validateArgsExprsArray(int indexOfMockField, Expression[] argExprs, VariableDefStmt finalVarDefStmt)
            throws BallerinaException {
        if (argExprs.length > indexOfMockField) {
            return;
        }

        throw new BallerinaException(
                MSG_PREFIX + "Index value should be less than the no. of arguments accepted by the connector '"
                        + finalVarDefStmt.getVariableDef().getName() + "': " + argExprs.length + " but is: " + (
                        indexOfMockField + 1) + ". Cannot update " + finalVarDefStmt.getVariableDef().getTypeName()
                        .getName() + " connector's arguments.");
    }

    private MockConnectorPath parseMockConnectorPath(Context ctx) {
        String mockCntrPathString = getArgument(ctx, 0).stringValue();
        String mockValue = getArgument(ctx, 1).stringValue();
        String[] mockCntrPathArr = mockCntrPathString.split("\\.");
        if (mockCntrPathArr.length < 2) {
            throw new BallerinaException(
                    "Error in parsing " + mockCntrPathString + ". Syntax: " + MOCK_PATH_SYNTAX + ". Example: "
                            + MOCK_PATH_SYNTAX_EXAMPLE);
        }

        String paramName = mockCntrPathArr[mockCntrPathArr.length - 1];

        //in case of multi-argument connector, we need a way to find the exact argument to update
        //for ballerina connectors, it can be derived via ParameterDefStmts of the connector
        //but for native connectors, it is not possible. Hence, user need to specify the index.
        //ex. helloService.myConnector.parameter2 ==> update the 2nd parameter of the connector
        int indexOfMockField = 0;
        Pattern pattern = Pattern.compile("^([0-9]*).+");
        Matcher matcher = pattern.matcher(new StringBuilder(paramName).reverse());
        if (matcher.matches()) {
            String group = new StringBuilder(matcher.group(1)).reverse().toString();
            if (!group.isEmpty()) {
                indexOfMockField = Integer.parseInt(group);
                indexOfMockField--; //user inputs for the parameter indexes begin from 1, but for us it's 0
            }
        }

        LinkedList<String> connectorNamesList = new LinkedList<>(
                Arrays.asList(Arrays.copyOfRange(mockCntrPathArr, 1, mockCntrPathArr.length - 1)));
        return new MockConnectorPath(mockCntrPathString, mockCntrPathArr[0], connectorNamesList, paramName, mockValue,
                indexOfMockField);
    }

    private VariableDefStmt getLastConnector(VariableDefStmt variableDefStmt, Queue<String> connectorNames) {
        BType con = variableDefStmt.getVariableDef().getType();
        String connectorNameToLookFor = connectorNames.poll();
        if (connectorNameToLookFor == null) {
            return variableDefStmt;
        }

        if (con instanceof BallerinaConnectorDef) {
            variableDefStmt = Arrays.stream(((BallerinaConnectorDef) con).getVariableDefStmts())
                    .filter(connector -> connector.getVariableDef().getName().equals(connectorNameToLookFor)).findAny()
                    .orElse(null);

            if (variableDefStmt == null) {
                throw new BallerinaException(COULD_NOT_FIND_MATCHING_CONNECTOR + connectorNameToLookFor);
            }
            //        } else if (con instanceof AbstractNativeConnector) {
            //todo use reflection to further traverse the tree
            //1) via reflection, get the list of all global variables that are of type Connector
            //2) verify the name I want is there.
            //3) if it is there, then get that instance and recurse further.
            //            throw new BallerinaException("error");
        }

        return getLastConnector(variableDefStmt, connectorNames);
    }

    /*
    private <T> T getField(Object instance, String fieldName, Class<T> fieldType) throws NoSuchFieldException {
        try {
            Field field = instance.getClass().getDeclaredField(fieldName);
            doPrivileged((PrivilegedAction<Object>) () -> {
                field.setAccessible(true);
                return null;
            });

            if (fieldType.isAssignableFrom(field.getType())) {
                return (T) field.get(instance);
            }
        } catch (IllegalAccessException e) {
            //ignore - not thrown since #isAssignableFrom is checked
        }
        throw new BallerinaException("Error while modifying native connector with the mock value.");
    }*/

    private void setProperty(Expression argExpr, String terminalVarName, String mockValue) {
        if (argExpr instanceof BasicLiteral) {
            BValue bValue = ((BasicLiteral) argExpr).getBValue();
            try {
                setProperty(bValue, terminalVarName, mockValue);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                //retrying with the default name
                try {
                    setProperty(bValue, FIELD_NAME_VALUE, mockValue);
                } catch (IllegalAccessException | NoSuchFieldException e1) {
                    throw new BallerinaException(
                            "Error while updating the field " + terminalVarName + " to " + mockValue);
                }
            }
        } else {
            throw new BallerinaException("ballerina: We can only process {@BasicLiteral}s. " + terminalVarName
                    + " is not a basic literal type");
            // todo handle expression types other than basic literal
        }
    }

    private <T> void setProperty(T instance, String fieldName, String value)
            throws IllegalAccessException, NoSuchFieldException {
        Field field = instance.getClass().getDeclaredField(fieldName);
        doPrivileged((PrivilegedAction<Object>) () -> {
            field.setAccessible(true);
            return null;
        });

        if (field.getType() == Character.TYPE) {
            field.set(instance, value.charAt(0));
            return;
        }
        if (field.getType() == Short.TYPE) {
            field.set(instance, Short.parseShort(value));
            return;
        }
        if (field.getType() == Integer.TYPE) {
            field.set(instance, Integer.parseInt(value));
            return;
        }
        if (field.getType() == Long.TYPE) {
            field.set(instance, Long.parseLong(value));
            return;
        }
        if (field.getType() == Double.TYPE) {
            field.set(instance, Double.parseDouble(value));
            return;
        }
        if (field.getType() == Float.TYPE) {
            field.set(instance, Float.parseFloat(value));
            return;
        }
        if (field.getType() == Byte.TYPE) {
            field.set(instance, Byte.parseByte(value));
            return;
        }
        if (field.getType() == Boolean.TYPE) {
            field.set(instance, Boolean.parseBoolean(value));
            return;
        }
        field.set(instance, value);
    }

    /**
     * This is the parsed model of the user's mockConnectorPath argument.
     */
    protected static class MockConnectorPath {
        String originalString;
        String serviceName;
        LinkedList<String> connectorNames;
        String terminalVarName;
        String mockValue;
        int indexOfMockField = 0;

        MockConnectorPath(String mockCntrPathString, String serviceName, LinkedList<String> connectorNames,
                String terminalVarName, String mockValue, int indexOfMockField) {
            this.originalString = mockCntrPathString;
            this.serviceName = serviceName;
            this.connectorNames = connectorNames;
            this.terminalVarName = terminalVarName;
            this.mockValue = mockValue;
            this.indexOfMockField = indexOfMockField;
        }
    }

}
