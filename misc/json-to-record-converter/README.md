# JSON to Record Converter

The tool is intended to map JSON values directly to Ballerina records without converting the JSON value to its schema. The tool individually analyzes the JSON value nodes and maps it to the Ballerina record either in-line or other. The JSON value is parsed as a parse tree using Google’s gson package. And the tree is traversed to generate the Ballerina record.

## Features
In the current implementation we support JSON value to Ballerina Record Conversion

##### _Example JSON Value_
```json
{
  "id": "0001",
  "type": "donut",
  "name": "Cake",
  "images": [
    {
      "url": "images/0001.jpg",
      "width": 200,
      "height": 200,
      "isTransparent": true
    },
    {
      "url": "images/0001.jpg",
      "width": 200,
      "height": 200
    }
  ],
  "thumbnail": {
    "url": "images/thumbnails/0001.jpg",
    "width": 32,
    "height": 32
  }
}
```

##### _Converted Ballerina Record_
```ballerina
type ImagesItem record {
    string url;
    int width;
    int height;
    boolean isTransparent?;
};

type Thumbnail record {
    string url;
    int width;
    int height;
};

type NewRecord record {
    string id;
    string 'type;
    string name;
    ImagesItem[] images;
    Thumbnail thumbnail;
};
```

##### _Converter Demo_
![alt text](./docs/images/converterDemo.gif?raw=true "Converter Demo")

## Use Case
JSON to Record converter API is primarily used by the [Ballerina vscode plugin](https://marketplace.visualstudio.com/items?itemName=WSO2.ballerina) to convert JSON value to Ballerina record interactively.

## JSON Value to Ballerina Record Mapping
### Introduction
This specification is for the JSON value to Ballerina Record mapping without interacting intermediate type conversion. Providing this util will increase the accuracy of the record.

### Overview
The tool is intended to map JSON values directly to Ballerina records without converting the JSON value to its schema. The tool individually analyzes the JSON value nodes and maps it to the Ballerina record either in-line or other. The JSON value is parsed as a parse tree using Google’s gson package. And the tree is traversed to generate the Ballerina record.

[GSON Documentation](https://www.javadoc.io/static/com.google.code.gson/gson/2.6.2/com/google/gson/JsonParser.html#parse-java.lang.String-)

### Type mapping
Parsing a JSON value returns a **_JsonElement_** as the root node. And **_JsonElement_** can be either of **_JsonObject_**, **_JsonPrimitive_**, **_JsonArray_** or **_JsonNull_**. And a **_JsonObject_** can be either of **_String_**, **_Number_** or **_Boolean_**.

The below table shows how the JSON Types are mapped to Java Classes and how that is mapped to Ballerina Types in generating Ballerina records from JSON values

<div>
    <table>
        <tbody>
            <tr>
                <td>Java Class</td>
                <td>JSON Type</td>
                <td>Ballerina Type&nbsp;</td>
            </tr>
            <tr>
                <td>JsonElement</td>
                <td>&nbsp;</td>
                <td>&nbsp;</td>
            </tr>
            <tr>
                <td>
                    <ul>
                        <li>JsonNull</li>
                    </ul>
                </td>
                <td>Null</td>
                <td>()/nil</td>
            </tr>
            <tr>
                <td>
                    <ul>
                        <li>JsonPrimitive</li>
                    </ul>
                </td>
                <td>&nbsp;</td>
                <td>&nbsp;</td>
            </tr>
            <tr>
                <td>
                    <ul>
                        <ul>
                            <li>isBoolean</li>
                        </ul>
                    </ul>
                </td>
                <td>Boolean</td>
                <td>boolean</td>
            </tr>
            <tr>
                <td>
                    <ul>
                        <ul>
                            <li>isString</li>
                        </ul>
                    </ul>
                </td>
                <td>String</td>
                <td>string</td>
            </tr>
            <tr>
                <td>
                    <ul>
                        <ul>
                            <li>isNumber (contains &lsquo;.&rsquo;)</li>
                        </ul>
                    </ul>
                </td>
                <td>Number</td>
                <td>decimal</td>
            </tr>
            <tr>
                <td>
                    <ul>
                        <ul>
                            <li>isNumber</li>
                        </ul>
                    </ul>
                </td>
                <td>Integer</td>
                <td>int</td>
            </tr>
            <tr>
                <td>
                    <ul>
                        <li>JsonObject</li>
                    </ul>
                </td>
                <td>Object</td>
                <td>record</td>
            </tr>
            <tr>
                <td>
                    <ul>
                        <li>JsonArray</li>
                    </ul>
                </td>
                <td>Array</td>
                <td>array</td>
            </tr>
        </tbody>
    </table>
    <p>&nbsp;</p>
</div>

### Mapping of Null and Optional Types
This section explains how different ways an optional Ballerina field can be generated.

#### Scenario 01: JSON null
When the value of a JSON field is null, it can be mapped as a field of [anydata](https://ballerina.io/spec/lang/master/#anydata) data type in ballerina.

JSON
```json
{
   "nullProperty": null
}
```
Ballerina
```ballerina
type NewRecord record {
   anydata nullProperty;
};
```

#### Scenario 02: Differencing Fields in the same JSON Object
When there are two or more JSON Objects with the same name, and if there are any differencing fields those fields will also be considered as optional fields.

JSON
```json
{
  "object": {
    "fieldOne": "StringValue",
    "fieldTwo": 16
  },
  "object": {
    "fieldOne": "StringValue"
  }
}
```
Ballerina
```ballerina
type NewRecord record {
   string fieldOne;
   int fieldTwo?;
};
```

In the above example, both JSON object fields are with the same name. And there is an intersecting field which is **_fieldOne_** and the differencing field **_fieldTwo_**. Thus, the intersecting field is made to be required and the differencing field is made to be optional.

### Mapping of Primitive Types
This section explains how JSON primitives can be mapped to Ballerina records. The JSON primitive types are String, Boolean and Number. Any JSON primitive type will be parsed as a **_JsonPrimitive_** Java object. And each object can be checked for String, Boolean or Number by calling the relevant method on the object.

#### Scenario  03: JSON string
JSON string can be mapped to ballerina string type.

JSON
```json
{
  "stringProperty": "StringValue"
}
```
Ballerina
```ballerina
type NewRecord record {
   string stringProperty;
};
```

#### Scenario  04: JSON boolean
JSON boolean can be mapped to ballerina boolean

JSON
```json
{
  "booleanProperty": true
}
```
Ballerina
```ballerina
type NewRecord record {
   boolean booleanProperty;
};
```

#### Scenario  05: JSON number
JSON Numbers can be either of below,
```json
{
  "positiveInteger": 210,
  "negativeInteger": -210,
  "floatingPointNumber": 21.05,
  "exponentialNumber": 2.105E+1
}
```
So, we have to map JSON numbers to either of Ballerina int, decimal or float based on the value. And, here we use all floating point numbers as decimals since, decimal is more appropriate to use since, decimals are more precise than floats and can store 128 bits
https://ballerina.io/spec/lang/master/#DecimalFloatingPointNumber

<div>
    <table>
        <tbody>
            <tr>
                <td><span style="color: #777777;">"</span><span style="color: #448c27;">positiveInteger</span><span style="color: #777777;">":</span><span style="color: #9c5d27;">210</span></td>
                <td><span style="color: #185e73;">int</span></td>
            </tr>
            <tr>
                <td><span style="color: #777777;">"</span><span style="color: #448c27;">negativeInteger</span><span style="color: #777777;">":</span><span style="color: #9c5d27;">-210</span></td>
                <td><span style="color: #185e73;">int</span></td>
            </tr>
            <tr>
                <td><span style="color: #777777;">"</span><span style="color: #448c27;">floatingPointNumber</span><span style="color: #777777;">":</span><span style="color: #9c5d27;">21.05</span></td>
                <td><span style="color: #185e73;">decimal</span></td>
            </tr>
            <tr>
                <td><span style="color: #777777;">"</span><span style="color: #448c27;">exponentialNumber</span><span style="color: #777777;">":</span><span style="color: #9c5d27;">2.105E+1</span></td>
                <td><span style="color: #185e73;">decimal</span></td>
            </tr>
        </tbody>
    </table>
</div>

JSON
```json
{
  "positiveInteger": 210,
  "negativeInteger": -210,
  "floatingPointNumber": 21.05,
  "exponentialNumber": 2.105E+1
}
```
Ballerina
```ballerina
type NewRecord record {
   int positiveInteger;
   int negativeInteger;
   decimal floatingPointNumber;
   decimal exponentialNumber;
};
```
Since both integers and decimals are classified as **_JsonPrimitive_** type, in the implementation level, Integers and decimals are classified based on the _point (.)_ character.

### Mapping of Object Types
This section explains how JSON objects can be mapped to Ballerina records. Each JSON object in the JSON value will be a new record or an inline record. Any JSON object will be parsed as a **_JsonObject_** Java object and will return a map with the field name as key and the object as value. And each JSON Object can have its child nodes as either of **_JsonObject, JsonPrimitive, JsonArray or JsonNull._**

#### Scenario 06: JSON Object
JSON
```json
{
  "recordProperty": {"name": "tom", "age": 5}
}
```
Ballerina
```ballerina
type RecordProperty record {
   string name;
   int age;
};
 
type NewRecord record {
   RecordProperty recordProperty;
};
```
There can be many JSON child objects with the same field name, those JSON objects will be mapped into a single Ballerina record where its intersecting fields are required Ballerina fields and the differencing fields are optional Ballerina fields. (This is explained in scenario 2)

#### Scenario 07: JSON Object with different field values
This is the scenario when there are two JSON child objects with the same field names, and the child objects have fields with the same name but different types of values.

JSON
```json
{
  "recordProperty": {"name": "tom", "age": 5},
  "recordProperty": {"name": "tom", "age": "tom"}
}
```
Ballerina
```ballerina
type RecordProperty record {
   string name;
   (int|string) age;
};
 
type NewRecord record {
   RecordProperty recordProperty;
};
```

### Mapping of Array Types

#### Scenario 08: JSON array without Items
Json Arrays are mapped into Ballerina arrays

JSON
```json
{
  "arrayProperty": []
}
```
Ballerina
```ballerina
type NewRecord record {
   anydata[] arrayProperty;
};
```
If the JSON array is empty, it means the array can contain any value which can be serializable. Thus, the Ballerina record field would have the data type of anydata which is the union of any serializable data types.

#### Scenario 09: JSON array of same type values
JSON
```json
{
  "arrayProperty": [10,20,40]
}
```
Ballerina
```ballerina
type NewRecord record {
   int[] arrayProperty;
};
```
This scenario can be applied for any type. No matter the type, if all values in the array are of the same type, then the record would get created with the field’s type as an array of the same type.

#### Scenario 10: JSON array with mixed value types (oneOf)
JSON
```json
{
  "arrayProperty": [10, 20, 40, "AU", "UK"]
}
```
Ballerina
```ballerina
type NewRecord record {
   (int|string)[] arrayProperty;
};
```
This is when there are more than one type of values present in the JSON array. The Ballerina record’s field type would be the union of the types of the values in the JSON array.

#### Scenario 11: JSON array with same JSON objects
JSON
```json
{
  "arrayProperty": [{"name": "tom", "age": 5}, {"name": "jerry", "age": 4}]
}
```
Ballerina
```ballerina
type ArrayPropertyItem record {
   string name;
   int age;
};
 
type NewRecord record {
   ArrayPropertyItem[] arrayProperty;
};
```
This scenario is when the JSON array only holds JSON objects and if all the objects schema is the same. If the Objects in the JSON array are different or if the array contains other types, then those are handled separately.

#### Scenario 12: JSON array with different JSON objects
In this case even if the objects in the array are completely different(the fields), still those objects will be treated the same with differencing fields having optional types (In such scenario all fields will become optional) See _Scenario 13_.

JSON
```json
{
  "arrayProperty": [{"name": "tom", "age": 5}, {"name": "jerry", "age": 4, "country": "SL"}]
}
```
Ballerina
```ballerina
type ArrayPropertyItem record {
   string name;
   int age;
   string country?;
};
 
type NewRecord record {
   ArrayPropertyItem arrayProperty;
};
```

#### Scenario 13: JSON Array completely different JSON objects
This is where all the fields are different in the objects in an array. (Even though the fields are different all these objects will be treated as same when converting to Ballerina record)

JSON
```json
{
  "arrayProperty": [{"name": "tom", "age": 5}, {"city": "Kandy", "zip": 71500}]
}
```
Ballerina
```ballerina
type ArrayProperty record {
   string name?;
   int age?;
   string city?;
   int zip?;
};
 
type NewRecord record {
   ArrayProperty[] arrayProperty;
};
```

#### Scenario 14: JSON Array with different types
JSON
```json
{
  "arrayProperty": [{"name": "tom", "age": 5}, "String Value", 5, 5.67]
}
```
Ballerina
```ballerina
type ArrayPropertyItem record {
   string name;
   int age?;
};
 
type NewRecordList record {
   (ArrayPropertyItem|decimal|int|string)[] arrayProperty;
};
```
In this case, if there are more than two types of values in an array. This will be handles as union of data types in Ballerina record creation.

#### Scenario 15: JSON Array of arrays
This is where Nested JSON arrays are being handled.

JSON
```json
{
  "arrayProperty": [[10, 20, 40], [10, 20, 18]]
}
```
Ballerina
```ballerina
type NewRecordList record {
   int[][] arrayProperty;
};
```
Here, the nested array can be any type, if the types are different, it would be handled differently. See _Scenario 17_

#### Scenario 16: JSON Array with different types and arrays
JSON
```json
{
  "arrayProperty": [{"name": "tom", "age": 5}, "String Value", 5, 5.67, [10, 20, 40], [["10", 20, 18]]]
}
```
Ballerina
```ballerina
type ArrayPropertyItem record {
   string name;
   int age?;
};
 
type NewRecordList record {
   (ArrayPropertyItem|decimal|int|string|int[]|(int|string)[][])[] arrayProperty;
};
```
The implementation can handle such complex scenarios as well. Here there are many values with different data types are handled. There is a nested array of different types within the array. Nested array of the same type. JSON object and JSON primitives. And all these types are handled when creating a Ballerina record by having the union of all these types.

## Contact Us
Managed By [WSO2 Inc.](https://wso2.com/)
Slack channel : [Ballerina Platform](https://ballerina-platform.slack.com/)
