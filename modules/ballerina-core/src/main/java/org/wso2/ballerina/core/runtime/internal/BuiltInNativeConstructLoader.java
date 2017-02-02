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

import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.model.Symbol;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.SymbolScope;
import org.wso2.ballerina.core.model.util.LangModelUtils;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeTypeConvertor;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaTypeConvertor;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeAction;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeConnector;
import org.wso2.ballerina.core.nativeimpl.connectors.http.client.Delete;
import org.wso2.ballerina.core.nativeimpl.connectors.http.client.Execute;
import org.wso2.ballerina.core.nativeimpl.connectors.http.client.Get;
import org.wso2.ballerina.core.nativeimpl.connectors.http.client.HTTPConnector;
import org.wso2.ballerina.core.nativeimpl.connectors.http.client.Patch;
import org.wso2.ballerina.core.nativeimpl.connectors.http.client.Post;
import org.wso2.ballerina.core.nativeimpl.connectors.http.client.Put;
import org.wso2.ballerina.core.nativeimpl.lang.array.DoubleArrayCopyOf;
import org.wso2.ballerina.core.nativeimpl.lang.array.DoubleArrayLength;
import org.wso2.ballerina.core.nativeimpl.lang.array.DoubleArrayRangeCopy;
import org.wso2.ballerina.core.nativeimpl.lang.array.FloatArrayCopyOf;
import org.wso2.ballerina.core.nativeimpl.lang.array.FloatArrayLength;
import org.wso2.ballerina.core.nativeimpl.lang.array.FloatArrayRangeCopy;
import org.wso2.ballerina.core.nativeimpl.lang.array.IntArrayCopyOf;
import org.wso2.ballerina.core.nativeimpl.lang.array.IntArrayLength;
import org.wso2.ballerina.core.nativeimpl.lang.array.IntArrayRangeCopy;
import org.wso2.ballerina.core.nativeimpl.lang.array.JsonArrayCopyOf;
import org.wso2.ballerina.core.nativeimpl.lang.array.JsonArrayLength;
import org.wso2.ballerina.core.nativeimpl.lang.array.JsonArrayRangeCopy;
import org.wso2.ballerina.core.nativeimpl.lang.array.LongArrayCopyOf;
import org.wso2.ballerina.core.nativeimpl.lang.array.LongArrayLength;
import org.wso2.ballerina.core.nativeimpl.lang.array.LongArrayRangeCopy;
import org.wso2.ballerina.core.nativeimpl.lang.array.MessageArrayCopyOf;
import org.wso2.ballerina.core.nativeimpl.lang.array.MessageArrayLength;
import org.wso2.ballerina.core.nativeimpl.lang.array.MessageArrayRangeCopy;
import org.wso2.ballerina.core.nativeimpl.lang.array.StringArrayCopyOf;
import org.wso2.ballerina.core.nativeimpl.lang.array.StringArrayLength;
import org.wso2.ballerina.core.nativeimpl.lang.array.StringArrayRangeCopy;
import org.wso2.ballerina.core.nativeimpl.lang.array.XmlArrayCopyOf;
import org.wso2.ballerina.core.nativeimpl.lang.array.XmlArrayLength;
import org.wso2.ballerina.core.nativeimpl.lang.array.XmlArrayRangeCopy;
import org.wso2.ballerina.core.nativeimpl.lang.convertors.JSONToString;
import org.wso2.ballerina.core.nativeimpl.lang.convertors.JSONToXML;
import org.wso2.ballerina.core.nativeimpl.lang.convertors.StringToJSON;
import org.wso2.ballerina.core.nativeimpl.lang.convertors.StringToXML;
import org.wso2.ballerina.core.nativeimpl.lang.convertors.XMLToJSON;
import org.wso2.ballerina.core.nativeimpl.lang.convertors.XMLToString;
import org.wso2.ballerina.core.nativeimpl.lang.json.AddBooleanToArray;
import org.wso2.ballerina.core.nativeimpl.lang.json.AddBooleanToObject;
import org.wso2.ballerina.core.nativeimpl.lang.json.AddDoubleToArray;
import org.wso2.ballerina.core.nativeimpl.lang.json.AddDoubleToObject;
import org.wso2.ballerina.core.nativeimpl.lang.json.AddFloatToArray;
import org.wso2.ballerina.core.nativeimpl.lang.json.AddFloatToObject;
import org.wso2.ballerina.core.nativeimpl.lang.json.AddIntToArray;
import org.wso2.ballerina.core.nativeimpl.lang.json.AddIntToObject;
import org.wso2.ballerina.core.nativeimpl.lang.json.AddJSONToArray;
import org.wso2.ballerina.core.nativeimpl.lang.json.AddJSONToObject;
import org.wso2.ballerina.core.nativeimpl.lang.json.AddStringToArray;
import org.wso2.ballerina.core.nativeimpl.lang.json.AddStringToObject;
import org.wso2.ballerina.core.nativeimpl.lang.json.GetBoolean;
import org.wso2.ballerina.core.nativeimpl.lang.json.GetDouble;
import org.wso2.ballerina.core.nativeimpl.lang.json.GetFloat;
import org.wso2.ballerina.core.nativeimpl.lang.json.GetInt;
import org.wso2.ballerina.core.nativeimpl.lang.json.GetJSON;
import org.wso2.ballerina.core.nativeimpl.lang.json.GetString;
import org.wso2.ballerina.core.nativeimpl.lang.json.Remove;
import org.wso2.ballerina.core.nativeimpl.lang.json.Rename;
import org.wso2.ballerina.core.nativeimpl.lang.json.SetBoolean;
import org.wso2.ballerina.core.nativeimpl.lang.json.SetDouble;
import org.wso2.ballerina.core.nativeimpl.lang.json.SetFloat;
import org.wso2.ballerina.core.nativeimpl.lang.json.SetInt;
import org.wso2.ballerina.core.nativeimpl.lang.json.SetJSON;
import org.wso2.ballerina.core.nativeimpl.lang.json.SetString;
import org.wso2.ballerina.core.nativeimpl.lang.json.ToString;
import org.wso2.ballerina.core.nativeimpl.lang.map.GetKeys;
import org.wso2.ballerina.core.nativeimpl.lang.message.AddHeader;
import org.wso2.ballerina.core.nativeimpl.lang.message.Clone;
import org.wso2.ballerina.core.nativeimpl.lang.message.GetHeader;
import org.wso2.ballerina.core.nativeimpl.lang.message.GetHeaders;
import org.wso2.ballerina.core.nativeimpl.lang.message.GetJsonPayload;
import org.wso2.ballerina.core.nativeimpl.lang.message.GetStringPayload;
import org.wso2.ballerina.core.nativeimpl.lang.message.GetXMLPayload;
import org.wso2.ballerina.core.nativeimpl.lang.message.RemoveHeader;
import org.wso2.ballerina.core.nativeimpl.lang.message.SetHeader;
import org.wso2.ballerina.core.nativeimpl.lang.message.SetJsonPayload;
import org.wso2.ballerina.core.nativeimpl.lang.message.SetStringPayload;
import org.wso2.ballerina.core.nativeimpl.lang.message.SetXMLPayload;
import org.wso2.ballerina.core.nativeimpl.lang.string.BooleanValueOf;
import org.wso2.ballerina.core.nativeimpl.lang.string.Contains;
import org.wso2.ballerina.core.nativeimpl.lang.string.DoubleValueOf;
import org.wso2.ballerina.core.nativeimpl.lang.string.EqualsIgnoreCase;
import org.wso2.ballerina.core.nativeimpl.lang.string.FloatValueOf;
import org.wso2.ballerina.core.nativeimpl.lang.string.HasPrefix;
import org.wso2.ballerina.core.nativeimpl.lang.string.HasSuffix;
import org.wso2.ballerina.core.nativeimpl.lang.string.IndexOf;
import org.wso2.ballerina.core.nativeimpl.lang.string.IntValueOf;
import org.wso2.ballerina.core.nativeimpl.lang.string.JsonValueOf;
import org.wso2.ballerina.core.nativeimpl.lang.string.LastIndexOf;
import org.wso2.ballerina.core.nativeimpl.lang.string.Length;
import org.wso2.ballerina.core.nativeimpl.lang.string.LongValueOf;
import org.wso2.ballerina.core.nativeimpl.lang.string.Replace;
import org.wso2.ballerina.core.nativeimpl.lang.string.ReplaceAll;
import org.wso2.ballerina.core.nativeimpl.lang.string.ReplaceFirst;
import org.wso2.ballerina.core.nativeimpl.lang.string.StringValueOf;
import org.wso2.ballerina.core.nativeimpl.lang.string.ToLowerCase;
import org.wso2.ballerina.core.nativeimpl.lang.string.ToUpperCase;
import org.wso2.ballerina.core.nativeimpl.lang.string.Trim;
import org.wso2.ballerina.core.nativeimpl.lang.string.Unescape;
import org.wso2.ballerina.core.nativeimpl.lang.string.XmlValueOf;
import org.wso2.ballerina.core.nativeimpl.lang.system.CurrentTimeMillis;
import org.wso2.ballerina.core.nativeimpl.lang.system.EpochTime;
import org.wso2.ballerina.core.nativeimpl.lang.system.LogBoolean;
import org.wso2.ballerina.core.nativeimpl.lang.system.LogDouble;
import org.wso2.ballerina.core.nativeimpl.lang.system.LogFloat;
import org.wso2.ballerina.core.nativeimpl.lang.system.LogInt;
import org.wso2.ballerina.core.nativeimpl.lang.system.LogLong;
import org.wso2.ballerina.core.nativeimpl.lang.system.LogString;
import org.wso2.ballerina.core.nativeimpl.lang.system.NanoTime;
import org.wso2.ballerina.core.nativeimpl.lang.system.PrintBoolean;
import org.wso2.ballerina.core.nativeimpl.lang.system.PrintDouble;
import org.wso2.ballerina.core.nativeimpl.lang.system.PrintFloat;
import org.wso2.ballerina.core.nativeimpl.lang.system.PrintInt;
import org.wso2.ballerina.core.nativeimpl.lang.system.PrintJSON;
import org.wso2.ballerina.core.nativeimpl.lang.system.PrintLong;
import org.wso2.ballerina.core.nativeimpl.lang.system.PrintString;
import org.wso2.ballerina.core.nativeimpl.lang.system.PrintXML;
import org.wso2.ballerina.core.nativeimpl.lang.system.PrintlnBoolean;
import org.wso2.ballerina.core.nativeimpl.lang.system.PrintlnDouble;
import org.wso2.ballerina.core.nativeimpl.lang.system.PrintlnFloat;
import org.wso2.ballerina.core.nativeimpl.lang.system.PrintlnInt;
import org.wso2.ballerina.core.nativeimpl.lang.system.PrintlnJSON;
import org.wso2.ballerina.core.nativeimpl.lang.system.PrintlnLong;
import org.wso2.ballerina.core.nativeimpl.lang.system.PrintlnString;
import org.wso2.ballerina.core.nativeimpl.lang.system.PrintlnXML;
import org.wso2.ballerina.core.nativeimpl.lang.xml.AddAttribute;
import org.wso2.ballerina.core.nativeimpl.lang.xml.AddAttributeWithNamespaces;
import org.wso2.ballerina.core.nativeimpl.lang.xml.AddElement;
import org.wso2.ballerina.core.nativeimpl.lang.xml.AddElementWithNamespaces;
import org.wso2.ballerina.core.nativeimpl.lang.xml.GetXML;
import org.wso2.ballerina.core.nativeimpl.lang.xml.GetXMLWithNamespaces;
import org.wso2.ballerina.core.nativeimpl.lang.xml.SetXML;
import org.wso2.ballerina.core.nativeimpl.lang.xml.SetXMLWithNamespaces;
import org.wso2.ballerina.core.nativeimpl.net.http.AcceptAndReturn;
import org.wso2.ballerina.core.nativeimpl.net.http.ConvertToResponse;
import org.wso2.ballerina.core.nativeimpl.net.http.GetContentLength;
import org.wso2.ballerina.core.nativeimpl.net.http.GetMethod;
import org.wso2.ballerina.core.nativeimpl.net.http.GetStatusCode;
import org.wso2.ballerina.core.nativeimpl.net.http.SetContentLength;
import org.wso2.ballerina.core.nativeimpl.net.http.SetReasonPhrase;
import org.wso2.ballerina.core.nativeimpl.net.http.SetStatusCode;
import org.wso2.ballerina.core.nativeimpl.net.uri.Encode;
import org.wso2.ballerina.core.nativeimpl.net.uri.GetQueryParam;
import org.wso2.ballerina.core.nativeimpl.util.GetHmac;
import org.wso2.ballerina.core.nativeimpl.util.GetRandomString;


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


    public static void loadConstructs(SymbolScope globalScope) {
        loadNativeFunctions(globalScope);
        loadNativeTypeConverters(globalScope);
    }

    public static void loadConstructs() {
    }

    /**
     * Load native functions to the runtime.
     */
    private static void loadNativeFunctions(SymbolScope globalScope) {

        //lang.array
        registerFunction(globalScope, new DoubleArrayCopyOf());
        registerFunction(globalScope, new DoubleArrayLength());
        registerFunction(globalScope, new DoubleArrayRangeCopy());
        registerFunction(globalScope, new FloatArrayCopyOf());
        registerFunction(globalScope, new FloatArrayLength());
        registerFunction(globalScope, new FloatArrayRangeCopy());
        registerFunction(globalScope, new IntArrayCopyOf());
        registerFunction(globalScope, new IntArrayLength());
        registerFunction(globalScope, new IntArrayRangeCopy());
        registerFunction(globalScope, new JsonArrayCopyOf());
        registerFunction(globalScope, new JsonArrayLength());
        registerFunction(globalScope, new JsonArrayRangeCopy());
        registerFunction(globalScope, new LongArrayCopyOf());
        registerFunction(globalScope, new LongArrayLength());
        registerFunction(globalScope, new LongArrayRangeCopy());
        registerFunction(globalScope, new MessageArrayCopyOf());
        registerFunction(globalScope, new MessageArrayLength());
        registerFunction(globalScope, new MessageArrayRangeCopy());
        registerFunction(globalScope, new StringArrayCopyOf());
        registerFunction(globalScope, new StringArrayLength());
        registerFunction(globalScope, new StringArrayRangeCopy());
        registerFunction(globalScope, new XmlArrayCopyOf());
        registerFunction(globalScope, new XmlArrayLength());
        registerFunction(globalScope, new XmlArrayRangeCopy());

        //lang.json
        registerFunction(globalScope, new AddBooleanToArray());
        registerFunction(globalScope, new AddBooleanToObject());
        registerFunction(globalScope, new AddDoubleToArray());
        registerFunction(globalScope, new AddDoubleToObject());
        registerFunction(globalScope, new AddFloatToArray());
        registerFunction(globalScope, new AddFloatToObject());
        registerFunction(globalScope, new AddIntToArray());
        registerFunction(globalScope, new AddIntToObject());
        registerFunction(globalScope, new AddJSONToArray());
        registerFunction(globalScope, new AddJSONToObject());
        registerFunction(globalScope, new AddStringToArray());
        registerFunction(globalScope, new AddStringToObject());
        registerFunction(globalScope, new GetBoolean());
        registerFunction(globalScope, new GetDouble());
        registerFunction(globalScope, new GetFloat());
        registerFunction(globalScope, new GetInt());
        registerFunction(globalScope, new GetJSON());
        registerFunction(globalScope, new GetString());
        registerFunction(globalScope, new Remove());
        registerFunction(globalScope, new Rename());
        registerFunction(globalScope, new SetBoolean());
        registerFunction(globalScope, new SetDouble());
        registerFunction(globalScope, new SetFloat());
        registerFunction(globalScope, new SetInt());
        registerFunction(globalScope, new SetJSON());
        registerFunction(globalScope, new SetString());
        registerFunction(globalScope, new ToString());

        //lang.message
        registerFunction(globalScope, new AddHeader());
        registerFunction(globalScope, new Clone());
        registerFunction(globalScope, new GetHeader());
        registerFunction(globalScope, new GetHeaders());
        registerFunction(globalScope, new GetJsonPayload());
        registerFunction(globalScope, new GetStringPayload());
        registerFunction(globalScope, new GetXMLPayload());
        registerFunction(globalScope, new RemoveHeader());
        registerFunction(globalScope, new SetHeader());
        registerFunction(globalScope, new SetJsonPayload());
        registerFunction(globalScope, new SetStringPayload());
        registerFunction(globalScope, new SetXMLPayload());

        // lang.string
        registerFunction(globalScope, new BooleanValueOf());
        registerFunction(globalScope, new Contains());
        registerFunction(globalScope, new DoubleValueOf());
        registerFunction(globalScope, new EqualsIgnoreCase());
        registerFunction(globalScope, new FloatValueOf());
        registerFunction(globalScope, new HasPrefix());
        registerFunction(globalScope, new HasSuffix());
        registerFunction(globalScope, new IndexOf());
        registerFunction(globalScope, new IntValueOf());
        registerFunction(globalScope, new JsonValueOf());
        registerFunction(globalScope, new LastIndexOf());
        registerFunction(globalScope, new Length());
        registerFunction(globalScope, new LongValueOf());
        registerFunction(globalScope, new Replace());
        registerFunction(globalScope, new ReplaceAll());
        registerFunction(globalScope, new ReplaceFirst());
        registerFunction(globalScope, new StringValueOf());
        registerFunction(globalScope, new ToLowerCase());
        registerFunction(globalScope, new ToUpperCase());
        registerFunction(globalScope, new Trim());
        registerFunction(globalScope, new Unescape());
        registerFunction(globalScope, new XmlValueOf());
        registerFunction(globalScope, new GetRandomString());

        // lang.system
        registerFunction(globalScope, new CurrentTimeMillis());
        registerFunction(globalScope, new EpochTime());
        registerFunction(globalScope, new LogBoolean());
        registerFunction(globalScope, new LogDouble());
        registerFunction(globalScope, new LogFloat());
        registerFunction(globalScope, new LogInt());
        registerFunction(globalScope, new LogLong());
        registerFunction(globalScope, new LogString());
        registerFunction(globalScope, new NanoTime());
        registerFunction(globalScope, new PrintBoolean());
        registerFunction(globalScope, new PrintDouble());
        registerFunction(globalScope, new PrintFloat());
        registerFunction(globalScope, new PrintInt());
        registerFunction(globalScope, new PrintlnBoolean());
        registerFunction(globalScope, new PrintlnDouble());
        registerFunction(globalScope, new PrintlnFloat());
        registerFunction(globalScope, new PrintlnInt());
        registerFunction(globalScope, new PrintlnLong());
        registerFunction(globalScope, new PrintlnString());
        registerFunction(globalScope, new PrintLong());
        registerFunction(globalScope, new PrintString());
        registerFunction(globalScope, new PrintJSON());
        registerFunction(globalScope, new PrintlnJSON());
        registerFunction(globalScope, new PrintXML());
        registerFunction(globalScope, new PrintlnXML());

        // lang.xml
        registerFunction(globalScope, new AddAttribute());
        registerFunction(globalScope, new AddAttributeWithNamespaces());
        registerFunction(globalScope, new AddElement());
        registerFunction(globalScope, new AddElementWithNamespaces());
        registerFunction(globalScope, new org.wso2.ballerina.core.nativeimpl.lang.xml.GetString());
        registerFunction(globalScope, new org.wso2.ballerina.core.nativeimpl.lang.xml.GetStringWithNamespaces());
        registerFunction(globalScope, new GetXML());
        registerFunction(globalScope, new GetXMLWithNamespaces());
        registerFunction(globalScope, new org.wso2.ballerina.core.nativeimpl.lang.xml.Remove());
        registerFunction(globalScope, new org.wso2.ballerina.core.nativeimpl.lang.xml.RemoveWithNamespaces());
        registerFunction(globalScope, new org.wso2.ballerina.core.nativeimpl.lang.xml.SetString());
        registerFunction(globalScope, new org.wso2.ballerina.core.nativeimpl.lang.xml.SetStringWithNamespaces());
        registerFunction(globalScope, new SetXML());
        registerFunction(globalScope, new SetXMLWithNamespaces());
        registerFunction(globalScope, new org.wso2.ballerina.core.nativeimpl.lang.xml.ToString());

        // lang.util
        registerFunction(globalScope, new GetHmac());

        // net.uri
        registerFunction(globalScope, new Encode());
        registerFunction(globalScope, new GetQueryParam());

        // net.http
        registerFunction(globalScope, new SetStatusCode());
        registerFunction(globalScope, new GetStatusCode());
        registerFunction(globalScope, new SetContentLength());
        registerFunction(globalScope, new GetContentLength());
        registerFunction(globalScope, new SetReasonPhrase());

        // lang.map
        registerFunction(globalScope, new GetKeys());
        registerFunction(globalScope, new org.wso2.ballerina.core.nativeimpl.lang.map.Length());
        registerFunction(globalScope, new org.wso2.ballerina.core.nativeimpl.lang.map.Remove());

        //http
        registerFunction(globalScope, new ConvertToResponse());
        registerFunction(globalScope, new GetMethod());
        registerFunction(globalScope, new AcceptAndReturn());

        registerConnector(globalScope, new HTTPConnector());

        registerAction(globalScope, new Get());
        registerAction(globalScope, new Post());
        registerAction(globalScope, new Put());
        registerAction(globalScope, new Delete());
        registerAction(globalScope, new Execute());
        registerAction(globalScope, new Patch());

    }

    /**
     * Add Native Function instance to given SymScope.
     *
     * @param symScope SymScope instance.
     * @param function Function instance.
     */
    private static void registerFunction(SymbolScope symScope, AbstractNativeFunction function) {

        BallerinaFunction functionNameAnnotation = function.getClass().getAnnotation(BallerinaFunction.class);
        if (functionNameAnnotation == null) {
            throw new BallerinaException("BallerinaFunction annotation not found");
        }

        SymbolName symbolName =
                LangModelUtils.getSymNameWithParams(function.getPackagePath() + ":" +
                        functionNameAnnotation.functionName(), function.getParameterDefs());
        symScope.define(symbolName, function);
    }

    /**
     * Register Native Action.
     *
     * @param action AbstractNativeAction instance.
     */
    public static void registerAction(SymbolScope symScope, AbstractNativeAction action) {
        String actionName = action.getSymbolName().getName();
        SymbolName symbolName = LangModelUtils.getSymNameWithParams(actionName, action.getParameterDefs());
        Symbol symbol = new Symbol(action);

        symScope.define(symbolName, action);
    }

    public static void registerConnector(SymbolScope symScope, AbstractNativeConnector connector) {
        org.wso2.ballerina.core.nativeimpl.annotations.BallerinaConnector connectorAnnotation =
                connector.getClass().getAnnotation(
                        org.wso2.ballerina.core.nativeimpl.annotations.BallerinaConnector.class);
        Symbol symbol = new Symbol(connector);

        SymbolName symbolName = new SymbolName(connectorAnnotation.packageName() + ":" +
                connectorAnnotation.connectorName());

        symScope.define(symbolName, connector);
    }

    private static void loadNativeTypeConverters(SymbolScope globalScope) {
        registerTypeConverter(globalScope, new JSONToXML());
        registerTypeConverter(globalScope, new XMLToJSON());
        registerTypeConverter(globalScope, new StringToJSON());
        registerTypeConverter(globalScope, new StringToXML());
        registerTypeConverter(globalScope, new XMLToString());
        registerTypeConverter(globalScope, new JSONToString());

    }

    /**
     * Add Native TypeConvertor instance to given SymScope.
     *
     * @param symScope SymScope instance.
     * @param typeConvertor TypeConvertor instance.
     */
    private static void registerTypeConverter(SymbolScope symScope, AbstractNativeTypeConvertor typeConvertor) {
        BallerinaTypeConvertor typeConverterNameAnnotation = typeConvertor.getClass()
                .getAnnotation(BallerinaTypeConvertor.class);
        if (typeConverterNameAnnotation == null) {
            throw new BallerinaException("BallerinaTypeConvertor annotation not found");
        }

        SymbolName symbolName =
                LangModelUtils.getTypeConverterSymName(typeConvertor.getPackagePath(), typeConvertor.getParameterDefs(),
                        typeConvertor.getReturnParameters());
        Symbol symbol = new Symbol(typeConvertor);
        symScope.define(symbolName, typeConvertor);

    }
}
