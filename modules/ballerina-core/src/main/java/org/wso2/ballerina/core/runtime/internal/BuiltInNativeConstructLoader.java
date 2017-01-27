/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.core.runtime.internal;

/**
 * {@code BuiltInNativeConstructLoader} is responsible for loading built-in native constructs in the ballerina core.
 * itself.
 * <p>
 * All the external native constructs are plugged into the core through osgi services.
 * Making built-in constructs also plugged through osgi increases the boot-up time.
 * That's the main reason for doing this in this fashion.
 *
 * @since 0.8.0
 */
public class BuiltInNativeConstructLoader {


    public static void loadConstructs() {
//        loadNativeFunctions();
//        loadNativeTypeConverters();
        // TODO
    }

/*
    private static void loadNativeFunctions() {
        SymScope scope = GlobalScopeHolder.getInstance().getScope();

        //lang.array
        registerFunction(scope, new DoubleArrayCopyOf());
        registerFunction(scope, new DoubleArrayLength());
        registerFunction(scope, new DoubleArrayRangeCopy());
        registerFunction(scope, new FloatArrayCopyOf());
        registerFunction(scope, new FloatArrayLength());
        registerFunction(scope, new FloatArrayRangeCopy());
        registerFunction(scope, new IntArrayCopyOf());
        registerFunction(scope, new IntArrayLength());
        registerFunction(scope, new IntArrayRangeCopy());
        registerFunction(scope, new JsonArrayCopyOf());
        registerFunction(scope, new JsonArrayLength());
        registerFunction(scope, new JsonArrayRangeCopy());
        registerFunction(scope, new LongArrayCopyOf());
        registerFunction(scope, new LongArrayLength());
        registerFunction(scope, new LongArrayRangeCopy());
        registerFunction(scope, new MessageArrayCopyOf());
        registerFunction(scope, new MessageArrayLength());
        registerFunction(scope, new MessageArrayRangeCopy());
        registerFunction(scope, new StringArrayCopyOf());
        registerFunction(scope, new StringArrayLength());
        registerFunction(scope, new StringArrayRangeCopy());
        registerFunction(scope, new XmlArrayCopyOf());
        registerFunction(scope, new XmlArrayLength());
        registerFunction(scope, new XmlArrayRangeCopy());

        //lang.json
        registerFunction(scope, new AddBooleanToArray());
        registerFunction(scope, new AddBooleanToObject());
        registerFunction(scope, new AddDoubleToArray());
        registerFunction(scope, new AddDoubleToObject());
        registerFunction(scope, new AddFloatToArray());
        registerFunction(scope, new AddFloatToObject());
        registerFunction(scope, new AddIntToArray());
        registerFunction(scope, new AddIntToObject());
        registerFunction(scope, new AddJSONToArray());
        registerFunction(scope, new AddJSONToObject());
        registerFunction(scope, new AddStringToArray());
        registerFunction(scope, new AddStringToObject());
        registerFunction(scope, new GetBoolean());
        registerFunction(scope, new GetDouble());
        registerFunction(scope, new GetFloat());
        registerFunction(scope, new GetInt());
        registerFunction(scope, new GetJSON());
        registerFunction(scope, new GetString());
        registerFunction(scope, new Remove());
        registerFunction(scope, new Rename());
        registerFunction(scope, new SetBoolean());
        registerFunction(scope, new SetDouble());
        registerFunction(scope, new SetFloat());
        registerFunction(scope, new SetInt());
        registerFunction(scope, new SetJSON());
        registerFunction(scope, new SetString());
        registerFunction(scope, new ToString());

        //lang.message
        registerFunction(scope, new AddHeader());
        registerFunction(scope, new Clone());
        registerFunction(scope, new GetHeader());
        registerFunction(scope, new GetHeaders());
        registerFunction(scope, new GetJsonPayload());
        registerFunction(scope, new GetStringPayload());
        registerFunction(scope, new GetXMLPayload());
        registerFunction(scope, new RemoveHeader());
        registerFunction(scope, new SetHeader());
        registerFunction(scope, new SetJsonPayload());
        registerFunction(scope, new SetStringPayload());
        registerFunction(scope, new SetXMLPayload());

        // lang.string
        registerFunction(scope, new BooleanValueOf());
        registerFunction(scope, new Contains());
        registerFunction(scope, new DoubleValueOf());
        registerFunction(scope, new EqualsIgnoreCase());
        registerFunction(scope, new FloatValueOf());
        registerFunction(scope, new HasPrefix());
        registerFunction(scope, new HasSuffix());
        registerFunction(scope, new IndexOf());
        registerFunction(scope, new IntValueOf());
        registerFunction(scope, new JsonValueOf());
        registerFunction(scope, new LastIndexOf());
        registerFunction(scope, new Length());
        registerFunction(scope, new LongValueOf());
        registerFunction(scope, new Replace());
        registerFunction(scope, new ReplaceAll());
        registerFunction(scope, new ReplaceFirst());
        registerFunction(scope, new StringValueOf());
        registerFunction(scope, new ToLowerCase());
        registerFunction(scope, new ToUpperCase());
        registerFunction(scope, new Trim());
        registerFunction(scope, new Unescape());
        registerFunction(scope, new XmlValueOf());
        registerFunction(scope, new GetRandomString());

        // lang.system
        registerFunction(scope, new CurrentTimeMillis());
        registerFunction(scope, new EpochTime());
        registerFunction(scope, new LogBoolean());
        registerFunction(scope, new LogDouble());
        registerFunction(scope, new LogFloat());
        registerFunction(scope, new LogInt());
        registerFunction(scope, new LogLong());
        registerFunction(scope, new LogString());
        registerFunction(scope, new NanoTime());
        registerFunction(scope, new PrintBoolean());
        registerFunction(scope, new PrintDouble());
        registerFunction(scope, new PrintFloat());
        registerFunction(scope, new PrintInt());
        registerFunction(scope, new PrintlnBoolean());
        registerFunction(scope, new PrintlnDouble());
        registerFunction(scope, new PrintlnFloat());
        registerFunction(scope, new PrintlnInt());
        registerFunction(scope, new PrintlnLong());
        registerFunction(scope, new PrintlnString());
        registerFunction(scope, new PrintLong());
        registerFunction(scope, new PrintString());
        registerFunction(scope, new PrintJSON());
        registerFunction(scope, new PrintlnJSON());
        registerFunction(scope, new PrintXML());
        registerFunction(scope, new PrintlnXML());

        // lang.xml
        registerFunction(scope, new AddAttribute());
        registerFunction(scope, new AddAttributeWithNamespaces());
        registerFunction(scope, new AddElement());
        registerFunction(scope, new AddElementWithNamespaces());
        registerFunction(scope, new org.wso2.ballerina.nativeimpl.lang.xml.GetString());
        registerFunction(scope, new org.wso2.ballerina.nativeimpl.lang.xml.GetStringWithNamespaces());
        registerFunction(scope, new GetXML());
        registerFunction(scope, new GetXMLWithNamespaces());
        registerFunction(scope, new org.wso2.ballerina.nativeimpl.lang.xml.Remove());
        registerFunction(scope, new org.wso2.ballerina.nativeimpl.lang.xml.RemoveWithNamespaces());
        registerFunction(scope, new org.wso2.ballerina.nativeimpl.lang.xml.SetString());
        registerFunction(scope, new org.wso2.ballerina.nativeimpl.lang.xml.SetStringWithNamespaces());
        registerFunction(scope, new SetXML());
        registerFunction(scope, new SetXMLWithNamespaces());
        registerFunction(scope, new org.wso2.ballerina.nativeimpl.lang.xml.ToString());

        // lang.util
        registerFunction(scope, new GetHmac());

        // net.uri
        registerFunction(scope, new Encode());
        registerFunction(scope, new GetQueryParam());

        // net.http
        registerFunction(scope, new SetStatusCode());
        registerFunction(scope, new GetStatusCode());
        registerFunction(scope, new SetContentLength());
        registerFunction(scope, new GetContentLength());
        registerFunction(scope, new SetReasonPhrase());

        // lang.map
        registerFunction(scope, new GetKeys());
        registerFunction(scope, new org.wso2.ballerina.nativeimpl.lang.map.Length());
        registerFunction(scope, new org.wso2.ballerina.nativeimpl.lang.map.Remove());

        //http
        registerFunction(scope, new ConvertToResponse());
        registerFunction(scope, new GetMethod());
        registerFunction(scope, new AcceptAndReturn());

        registerConnector(scope, new HTTPConnector());

        registerAction(scope, new Get());
        registerAction(scope, new Post());
        registerAction(scope, new Put());
        registerAction(scope, new Delete());
        registerAction(scope, new Execute());
        registerAction(scope, new Patch());

    }
*/

    /**
     * Add Native Function instance to given SymScope.
     *
     * @param symScope SymScope instance.
     * @param function Function instance.
     */
    /*private static void registerFunction(SymScope symScope, AbstractNativeFunction function) {

        BallerinaFunction functionNameAnnotation = function.getClass().getAnnotation(BallerinaFunction.class);
        if (functionNameAnnotation == null) {
            throw new BallerinaException("BallerinaFunction annotation not found");
        }

        SymbolName symbolName =
                LangModelUtils.getSymNameWithParams(function.getPackageName() + ":" +
                        functionNameAnnotation.functionName(), function.getParameters());
        Symbol symbol = new Symbol(function);
        symScope.insert(symbolName, symbol);
    }*/

    /**
     * Register Native Action.
     *
     * @param action AbstractNativeAction instance.
     */
    /*public static void registerAction(SymScope symScope, AbstractNativeAction action) {
        String actionName = action.getSymbolName().getName();
        SymbolName symbolName = LangModelUtils.getSymNameWithParams(actionName, action.getParameters());
        Symbol symbol = new Symbol(action);

        symScope.insert(symbolName, symbol);
    }

    public static void registerConnector(SymScope symScope, AbstractNativeConnector connector) {
        org.wso2.ballerina.core.nativeimpl.annotations.BallerinaConnector connectorAnnotation =
                connector.getClass().getAnnotation(
                        org.wso2.ballerina.core.nativeimpl.annotations.BallerinaConnector.class);
        Symbol symbol = new Symbol(connector);

        SymbolName symbolName = new SymbolName(connectorAnnotation.packageName() + ":" +
                connectorAnnotation.connectorName());

        symScope.insert(symbolName, symbol);
    }

    private static void loadNativeTypeConverters() {
        SymScope scope = GlobalScopeHolder.getInstance().getScope();

        registerTypeConverter(scope, new JSONToXML());
        registerTypeConverter(scope, new XMLToJSON());
        registerTypeConverter(scope, new StringToJSON());
        registerTypeConverter(scope, new StringToXML());
        registerTypeConverter(scope, new XMLToString());
        registerTypeConverter(scope, new JSONToString());

    }*/

    /**
     * Add Native TypeConvertor instance to given SymScope.
     *
     * @param symScope SymScope instance.
     * @param typeConvertor TypeConvertor instance.
     */
    /*private static void registerTypeConverter(SymScope symScope, AbstractNativeTypeConvertor typeConvertor) {
        BallerinaTypeConvertor typeConverterNameAnnotation = typeConvertor.getClass()
                .getAnnotation(BallerinaTypeConvertor.class);
        if (typeConverterNameAnnotation == null) {
            throw new BallerinaException("BallerinaTypeConvertor annotation not found");
        }

        SymbolName symbolName =
                LangModelUtils.getTypeConverterSymName(typeConvertor.getPackageName(), typeConvertor.getParameters(),
                        typeConvertor.getReturnParameters());
        Symbol symbol = new Symbol(typeConvertor);
        symScope.insert(symbolName, symbol);

    }*/
}
