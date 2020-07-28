# Ballerina Intermediate Representation (BIR) Specification

## BIR usage in compiler

```
                                                                                                           ,-> JVM Code gen
Lexer/Parser -> Type Check -> Semantic analyzer -> Desugar -> BIR gen -> BIR instrumentation/optimization -
                                                                                                           `-> LLVM Code gen
```

Above `BIR gen` phase outputs the BIR format that will be used by the later phases.
By default BIR is represented as an internal object model, via set of sub classes of
`org.wso2.ballerinalang.compiler.bir.model.BIRNode` class and `org.wso2.ballerinalang.compiler.bir.model.BIRInstruction`
class.

You may view this in text format by passing `--dump-bir` flag to the `ballerina build` command or save it to a binary
file by passing `--dump-bir-file /location/to/save.bir`

## Goals of BIR

1. Support multiple backend implementations.

2. Help ballerina compiler writes as a diagnostic tool.

3. Act as the internal format for the optimizer and instrumentation.


## Bir binary file structure

Binary file constant of two main areas, Constant Pool and the Module section.

### Constant Pool

Constant pool contains entry types identified by following tags


* cp_entry_integer - 1 
* cp_entry_float - 2 
* cp_entry_boolean - 3 
* cp_entry_string - 4 
* cp_entry_package - 5 
* cp_entry_byte - 6 
* cp_entry_shape - 7 

### Types

Ballerina types are serialized with following type tags

* type_tag_int - 1 - Basic type, 64-bit signed integers
* type_tag_byte - 2
* type_tag_float - 3
* type_tag_decimal - 4
* type_tag_string - 5
* type_tag_boolean - 6
* type_tag_json - 7
* type_tag_xml - 8
* type_tag_table - 9
* type_tag_nil - 10
* type_tag_anydata - 11
* type_tag_record - 12
* type_tag_typedesc - 13
* type_tag_stream - 14
* type_tag_map - 15
* type_tag_invokable - 16
* type_tag_any - 17
* type_tag_endpoint - 18
* type_tag_array - 19
* type_tag_union - 20
* type_tag_intersection - 21
* type_tag_package - 22
* type_tag_none - 23
* type_tag_void - 24
* type_tag_xmlns - 25
* type_tag_annotation - 26
* type_tag_semantic_error - 27
* type_tag_error - 28
* type_tag_iterator - 29
* type_tag_tuple - 30
* type_tag_future - 31
* type_tag_finite - 32
* type_tag_object - 33
* type_tag_service - 34
* type_tag_byte_array - 35
* type_tag_function_pointer - 36
* type_tag_handle - 37
* type_tag_readonly - 38
* type_tag_signed32_int - 39
* type_tag_signed16_int - 40
* type_tag_signed8_int - 41
* type_tag_unsigned32_int - 42
* type_tag_unsigned16_int - 43
* type_tag_unsigned8_int - 44
* type_tag_char_string - 45
* type_tag_xml_element - 46
* type_tag_xml_pi - 47
* type_tag_xml_comment - 48
* type_tag_xml_text - 49
* type_tag_never - 50
* type_tag_null_set - 51
* type_tag_parameterized_type - 52


