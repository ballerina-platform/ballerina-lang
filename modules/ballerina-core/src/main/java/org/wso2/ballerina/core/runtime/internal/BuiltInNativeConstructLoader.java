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
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.Symbol;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.util.LangModelUtils;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
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
import org.wso2.ballerina.core.nativeimpl.lang.string.SubString;
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


    public static void loadConstructs() {
        loadNativeFunctions();
    }

    /**
     * Load native functions to the runtime.
     */
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
        registerFunction(scope, new SubString());
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
        registerFunction(scope, new org.wso2.ballerina.core.nativeimpl.lang.xml.GetString());
        registerFunction(scope, new org.wso2.ballerina.core.nativeimpl.lang.xml.GetStringWithNamespaces());
        registerFunction(scope, new GetXML());
        registerFunction(scope, new GetXMLWithNamespaces());
        registerFunction(scope, new org.wso2.ballerina.core.nativeimpl.lang.xml.Remove());
        registerFunction(scope, new org.wso2.ballerina.core.nativeimpl.lang.xml.RemoveWithNamespaces());
        registerFunction(scope, new org.wso2.ballerina.core.nativeimpl.lang.xml.SetString());
        registerFunction(scope, new org.wso2.ballerina.core.nativeimpl.lang.xml.SetStringWithNamespaces());
        registerFunction(scope, new SetXML());
        registerFunction(scope, new SetXMLWithNamespaces());
        registerFunction(scope, new org.wso2.ballerina.core.nativeimpl.lang.xml.ToString());

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
        registerFunction(scope, new org.wso2.ballerina.core.nativeimpl.lang.map.Length());
        registerFunction(scope, new org.wso2.ballerina.core.nativeimpl.lang.map.Remove());

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

    /**
     * Add Native Function instance to given SymScope.
     *
     * @param symScope SymScope instance.
     * @param function Function instance.
     */
    private static void registerFunction(SymScope symScope, AbstractNativeFunction function) {

        BallerinaFunction functionNameAnnotation = function.getClass().getAnnotation(BallerinaFunction.class);
        if (functionNameAnnotation == null) {
            throw new BallerinaException("BallerinaFunction annotation not found");
        }

        SymbolName symbolName =
                LangModelUtils.getSymNameWithParams(function.getPackageName() + ":" +
                        functionNameAnnotation.functionName(), function.getParameters());
        Symbol symbol = new Symbol(function);
        symScope.insert(symbolName, symbol);
    }

    /**
     * Register Native Action.
     *
     * @param action AbstractNativeAction instance.
     */
    public static void registerAction(SymScope symScope, AbstractNativeAction action) {
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

}
