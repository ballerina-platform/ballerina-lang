# JSON to record converter

The tool is intended to map JSON values directly to Ballerina records without converting the JSON value to its schema. The tool analyzes the JSON value nodes individually and maps them to Ballerina records. The JSON value is parsed as a parse tree using [Google’s `Gson` package](https://www.javadoc.io/static/com.google.code.gson/gson/2.6.2/com/google/gson/JsonParser.html#parse-java.lang.String-), and the tree is traversed to generate the Ballerina record.

## Features
The current implementation supports converting JSON values to Ballerina records.

### Example
**The JSON value**
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

**Converted Ballerina record**
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

**Converter Demo**

This video explains how to use the JSON to Ballerina record converter tool in VSCode, when [Ballerina VS Code plugin](https://marketplace.visualstudio.com/items?itemName=WSO2.ballerina) is installed.

![alt text](./docs/images/converterDemo.gif?raw=true "Converter Demo")

## Use case
JSON to Record converter API is primarily used by the [Ballerina VS Code plugin](https://marketplace.visualstudio.com/items?itemName=WSO2.ballerina) to convert JSON values to Ballerina records interactively.

## JSON value to Ballerina record mapping

### Introduction
This specification is for the JSON value to Ballerina record mapping without interacting intermediate type conversion. Providing this util will increase the accuracy of the record.

### Type mapping
Parsing a JSON value returns a `JsonElement` as the root node and the `JsonElement` can be either a `JsonObject`, `JsonPrimitive`, `JsonArray`, or `JsonNull`. A `JsonObject` can be either a `String`, `Number`, or `Boolean`.

The table below shows how the JSON types are mapped to Java classes and how they are mapped to Ballerina types when generating Ballerina records from JSON values.

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

### Mapping of null and optional types
This section explains the different ways by which an optional Ballerina field can be generated.

#### JSON null
When the value of a JSON field is null, it can be mapped as a field of [anydata](https://ballerina.io/spec/lang/master/#anydata) data type in ballerina.

```json
{
   "nullProperty": null
}
```

```ballerina
type NewRecord record {
   anydata nullProperty;
};
```

#### Optional fields within the same JSON object
When there are two or more JSON objects with the same name, and if there are any different fields, those fields will also be considered optional fields.

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

```ballerina
type NewRecord record {
   string fieldOne;
   int fieldTwo?;
};
```

In the above example, both JSON object fields have the same name. Also, there is an intersecting field, which is `fieldOne` and the different field is `fieldTwo`. Therefore, the intersecting field is made to be required and the different field is made to be optional.

### Mapping of primitive types
This section explains how JSON primitives can be mapped to Ballerina records. The JSON primitive types are `String`, `Boolean`, and `Number`. Any JSON primitive type will be parsed as a `JsonPrimitive` Java object. Each object can be checked for `String`, `Boolean`, or `Number` by calling the relevant method on the object.

#### JSON string
JSON string can be mapped to Ballerina string type.

```json
{
  "stringProperty": "StringValue"
}
```

```ballerina
type NewRecord record {
   string stringProperty;
};
```

#### JSON boolean
JSON boolean can be mapped to a Ballerina boolean.

```json
{
  "booleanProperty": true
}
```

```ballerina
type NewRecord record {
   boolean booleanProperty;
};
```

#### JSON number

JSON numbers should have to be mapped to either of the Ballerina int, decimal, or float types based on the value. Here, all floating point numbers are used as decimals since decimal is more appropriate to use and also because decimals are more precise than floats and can store 128 bits.
https://ballerina.io/spec/lang/master/#DecimalFloatingPointNumber

JSON numbers can be either of the below.
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


```json
{
  "positiveInteger": 210,
  "negativeInteger": -210,
  "floatingPointNumber": 21.05,
  "exponentialNumber": 2.105E+1
}
```

```ballerina
type NewRecord record {
   int positiveInteger;
   int negativeInteger;
   decimal floatingPointNumber;
   decimal exponentialNumber;
};
```
Since both integers and decimals are classified as `JsonPrimitive` type, at the implementation level, `Integers` and `decimals` are classified based on the `point (.)` character.

### Mapping of object types
This section explains how JSON objects can be mapped to Ballerina records. Each JSON object in the JSON value will be a new record or an inline record. Any JSON object will be parsed as a `JsonObject` Java object and will return a map with the field name as key and the object as the value. Each JSON Object can have its child nodes as either of `JsonObject`, `JsonPrimitive`, `JsonArray` or `JsonNull`.

#### JSON object

```json
{
  "recordProperty": {"name": "tom", "age": 5}
}
```

```ballerina
type RecordProperty record {
   string name;
   int age;
};
 
type NewRecord record {
   RecordProperty recordProperty;
};
```
There can be many JSON child objects with the same field name. Those JSON objects will be mapped into a single Ballerina record where its intersecting fields are required Ballerina fields and the different fields are optional Ballerina fields. (This is explained in the section [Optional fields within the same JSON object](#optional-fields-within-the-same-json-object))

#### JSON object with different field values
This scenario has two JSON child objects with the same field names and the child objects have fields with the same name but different types of values.

```json
{
  "recordProperty": {"name": "tom", "age": 5},
  "recordProperty": {"name": "tom", "age": "tom"}
}
```

```ballerina
type RecordProperty record {
   string name;
   (int|string) age;
};
 
type NewRecord record {
   RecordProperty recordProperty;
};
```

### Mapping of array types

#### JSON array without items
JSON arrays are mapped into Ballerina arrays

```json
{
  "arrayProperty": []
}
```

```ballerina
type NewRecord record {
   anydata[] arrayProperty;
};
```
If the JSON array is empty, it means that the array can contain any value, which can be serializable. Thus, the Ballerina record field would have the data type of anydata, which is the union of any serializable data types.

#### JSON array of same type values

```json
{
  "arrayProperty": [10,20,40]
}
```

```ballerina
type NewRecord record {
   int[] arrayProperty;
};
```
This scenario can be applied to any type. For any type, if all values in the array are of the same type, then, the record will get created with the field’s type as an array of the same type.

#### JSON array with mixed value types (`oneOf`)

```json
{
  "arrayProperty": [10, 20, 40, "AU", "UK"]
}
```

```ballerina
type NewRecord record {
   (int|string)[] arrayProperty;
};
```
This is when there are more than one type of values present in the JSON array. The Ballerina record’s field type would be the union of the types of the values in the JSON array.

#### JSON array with same JSON objects

```json
{
  "arrayProperty": [{"name": "tom", "age": 5}, {"name": "jerry", "age": 4}]
}
```

```ballerina
type ArrayPropertyItem record {
   string name;
   int age;
};
 
type NewRecord record {
   ArrayPropertyItem[] arrayProperty;
};
```
In this scenario, the JSON array only holds JSON objects, and if the schema of all the objects is the same. If the objects in the JSON array are different or if the array contains other types, then, those are handled separately.

#### JSON array with different JSON objects
In this scenario, even if the objects in the array (the fields) are completely different, still those objects will be treated the same with different fields having optional types (in such scenarios, all fields will become optional) . For example, see [JSON array with completely different JSON objects](#json-array-with-completely-different-json-objects).

```json
{
  "arrayProperty": [{"name": "tom", "age": 5}, {"name": "jerry", "age": 4, "country": "SL"}]
}
```

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

#### JSON array with completely different JSON objects
This scenario has all different fields in the objects in an array (even though the fields are different, all these objects will be treated as the same when converting to Ballerina records).

```json
{
  "arrayProperty": [{"name": "tom", "age": 5}, {"city": "Kandy", "zip": 71500}]
}
```

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

#### JSON array with different types

```json
{
  "arrayProperty": [{"name": "tom", "age": 5}, "String Value", 5, 5.67]
}
```

```ballerina
type ArrayPropertyItem record {
   string name;
   int age?;
};
 
type NewRecordList record {
   (ArrayPropertyItem|decimal|int|string)[] arrayProperty;
};
```
This scenario has more than two types of values in an array. This will be handled as the union of data types in the Ballerina record creation.

#### JSON array of arrays
This is where nested JSON arrays are being handled.

```json
{
  "arrayProperty": [[10, 20, 40], [10, 20, 18]]
}
```

```ballerina
type NewRecordList record {
   int[][] arrayProperty;
};
```
Here, the nested array can be any type. If the types are different, it would be handled differently. For example, see [JSON array with different types and arrays](#json-array-with-different-types-and-arrays).

#### JSON array with different types and arrays

```json
{
  "arrayProperty": [{"name": "tom", "age": 5}, "String Value", 5, 5.67, [10, 20, 40], [["10", 20, 18]]]
}
```

```ballerina
type ArrayPropertyItem record {
   string name;
   int age?;
};
 
type NewRecordList record {
   (ArrayPropertyItem|decimal|int|string|int[]|(int|string)[][])[] arrayProperty;
};
```
The implementation can handle complex scenarios having many values with different data types as well. The above scenario has a nested array of different types within the array. The nested array is of the same type as the JSON object and JSON primitives. All these types are handled when creating a Ballerina record by having the union of all these types.

## Contact Us
Managed By [WSO2 Inc.](https://wso2.com/)
Discord server: [Ballerina](https://discord.gg/ballerinalang)
