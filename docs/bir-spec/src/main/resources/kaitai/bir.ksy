meta:
  id: bir
  file-extension: bir
  endian: be
  license: Apache 2.0
doc-ref: https://github.com/ballerina-platform/ballerina-lang/blob/master/docs/compiler/bir-spec.md
seq:
  - id: constant_pool
    type: constant_pool_set
  - id: module
    type: module
enums:
  type_tag_enum:
    1:
      id: type_tag_int
      doc: Basic type, 64-bit signed integers
    2: type_tag_byte
    3: type_tag_float
    4: type_tag_decimal
    5: type_tag_string
    6: type_tag_boolean
    7: type_tag_json
    8: type_tag_xml
    9: type_tag_table
    10: type_tag_nil
    11: type_tag_anydata
    12: type_tag_record
    13: type_tag_typedesc
    14: type_tag_stream
    15: type_tag_map
    16: type_tag_invokable
    17: type_tag_any
    18: type_tag_endpoint
    19: type_tag_array
    20: type_tag_union
    21: type_tag_intersection
    22: type_tag_package
    23: type_tag_none
    24: type_tag_void
    25: type_tag_xmlns
    26: type_tag_annotation
    27: type_tag_semantic_error
    28: type_tag_error
    29: type_tag_iterator
    30: type_tag_tuple
    31: type_tag_future
    32: type_tag_finite
    33: type_tag_object
    34: type_tag_service
    35: type_tag_byte_array
    36: type_tag_function_pointer
    37: type_tag_handle
    38: type_tag_readonly
    39: type_tag_signed32_int
    40: type_tag_signed16_int
    41: type_tag_signed8_int
    42: type_tag_unsigned32_int
    43: type_tag_unsigned16_int
    44: type_tag_unsigned8_int
    45: type_tag_char_string
    46: type_tag_xml_element
    47: type_tag_xml_pi
    48: type_tag_xml_comment
    49: type_tag_xml_text
    50: type_tag_never
    51: type_tag_null_set
    52: type_tag_parameterized_type
types:
  constant_pool_set:
    seq:
      - id: constant_pool_count
        type: s4
      - id: constant_pool_entries
        type: constant_pool_entry
        repeat: expr
        repeat-expr: constant_pool_count
  constant_pool_entry:
    seq:
      - id: tag
        type: u1
        enum: tag_enum
      - id: cp_info
        type:
          switch-on: tag
          cases:
            'tag_enum::cp_entry_integer': int_cp_info
            'tag_enum::cp_entry_float': float_cp_info
            'tag_enum::cp_entry_boolean': boolean_cp_info
            'tag_enum::cp_entry_string': string_cp_info
            'tag_enum::cp_entry_package': package_cp_info
            'tag_enum::cp_entry_byte': byte_cp_info
            'tag_enum::cp_entry_shape': shape_cp_info
    enums:
      tag_enum:
        1: cp_entry_integer
        2: cp_entry_float
        3: cp_entry_boolean
        4: cp_entry_string
        5: cp_entry_package
        6: cp_entry_byte
        7: cp_entry_shape
  int_cp_info:
    seq:
      - id: value
        type: s8
  float_cp_info:
    seq:
      - id: value
        type: f8
  boolean_cp_info:
    seq:
      - id: value
        type: u1
  string_cp_info:
    seq:
      - id: str_len
        type: s4
      - id: value
        type: str
        size: str_len
        encoding: UTF-8
  package_cp_info:
    seq:
      - id: org_index
        type: s4
      - id: name_index
        type: s4
      - id: version_index
        type: s4
  byte_cp_info:
    seq:
      - id: value
        type: s4
  shape_cp_info:
    seq:
      - id: shape_lenght
        type: s4
      - id: shape
        size: shape_lenght
        type: type_info
  type_info:
    seq:
      - id: type_tag
        type: s1
        enum: type_tag_enum
      - id: name_index
        type: s4
      - id: type_flag
        type: s4
      - id: type_special_flag
        type: s4
      - id: type_structure
        type:
          switch-on: type_tag
          cases:
            'type_tag_enum::type_tag_invokable': type_invokable
    instances:
      name_as_str:
        value: _root.constant_pool.constant_pool_entries[name_index].cp_info.as<string_cp_info>.value
  type_invokable:
    seq:
      - id: param_count
        type: s4
      - id: has_rest_type
        type: u1
      - id: return_type_index
        type: s4
  module:
    seq:
      - id: id_cp_index
        type: s4
      - id: import_count
        type: s4
      - id: imports
        type: package_cp_info
        repeat: expr
        repeat-expr: import_count
      - id: const_count
        type: s4
      - id: constants
        type: constant
        repeat: expr
        repeat-expr: const_count
      - id: type_definition_count
        type: s4
      - id: type_definitions
        type: type_definition
        repeat: expr
        repeat-expr: type_definition_count
      - id: golbal_var_count
        type: s4
      - id: golbal_vars
        type: golbal_var
        repeat: expr
        repeat-expr: golbal_var_count
      - id: type_definition_bodies_count
        type: s4
      - id: type_definition_bodies
        type: type_definition_body
        repeat: expr
        repeat-expr: type_definition_bodies_count
      - id: function_count
        type: s4
      - id: functions
        type: function
        repeat: expr
        repeat-expr: function_count
      - id: annotations_size
        type: s4
  golbal_var:
    seq:
      - id: kind
        type: s1
      - id: name_cp_index
        type: s4
      - id: flags
        type: s4
      - id: doc
        type: markdown
      - id: type_cp_index
        type: s4
  type_definition:
    seq:
      - id: position
        type: position
      - id: name_cp_index
        type: s4
      - id: flags
        type: s4
      - id: label
        type: s1
      - id: doc
        type: markdown
      - id: type_cp_index
        type: s4
  type_definition_body:
    seq:
      - id: attached_functions_count
        type: s4
      - id: attached_functions
        type: function
        repeat: expr
        repeat-expr: attached_functions_count
      - id: referenced_types_count
        type: s4
      - id: referenced_types
        type: referenced_type
        repeat: expr
        repeat-expr: referenced_types_count
  constant:
    seq:
      - id: name_cp_index
        type: s4
      - id: flags
        type: s4
      - id: doc
        type: markdown
      - id: type_cp_index
        type: s4
      - id: length
        type: s8
      - id: constant_value
        type: constant_value
  constant_value:
    seq:
      - id: constant_value_type_cp_index
        type: s4
      - id: constant_value_info
        type:
          switch-on: type.shape.type_tag
          cases:
            'type_tag_enum::type_tag_int': int_constant_info
            'type_tag_enum::type_tag_byte': byte_constant_info
            'type_tag_enum::type_tag_float': float_constant_info
            'type_tag_enum::type_tag_string': string_constant_info
            'type_tag_enum::type_tag_decimal': decimal_constant_info
            'type_tag_enum::type_tag_boolean': boolean_constant_info
            'type_tag_enum::type_tag_nil': nil_constant_info
            'type_tag_enum::type_tag_map': map_constant_info
    instances:
      type:
        value: _root.constant_pool.constant_pool_entries[constant_value_type_cp_index].cp_info.as<shape_cp_info>
  int_constant_info:
    seq:
      - id: value_cp_index
        type: s4
  byte_constant_info:
    seq:
      - id: value_cp_index
        type: s4
  float_constant_info:
    seq:
      - id: value_cp_index
        type: s4
  string_constant_info:
    seq:
      - id: value_cp_index
        type: s4
  decimal_constant_info:
    seq:
      - id: value_cp_index
        type: s4
  boolean_constant_info:
    seq:
      - id: value_boolean_constant
        type: s2
  nil_constant_info:
    seq:
      - id: value_nil_constant
        size: 0
  map_constant_info:
    seq:
      - id: map_constant_size
        type: s4
      - id: map_key_values
        type: map_key_value
        repeat: expr
        repeat-expr: map_constant_size
  map_key_value:
    seq:
      - id: key_name_cp_index
        type: s4
      - id: key_value_info
        type: constant_value
  markdown:
    seq:
      - id: length
        type: s4
      - id: has_doc
        type: markdown_content
        size: length
  markdown_content:
    seq:
      - id: has_doc
        type: u1
  function:
    seq:
      - id: position
        type: position
      - id: name_cp_index
        type: s4
      - id: worker_name_cp_index
        type: s4
      - id: flags
        type: s4
      - id: type_cp_index
        type: s4
      - id: annotation_attachments_content_length
        type: s8
      - id: annotation_attachments
        size: annotation_attachments_content_length
      - id: required_param_count
        type: s4
      - id: required_params
        type: required_param
        repeat: expr
        repeat-expr: required_param_count
      - id: has_rest_param
        type: u1
      - id: rest_param_name_cp_index
        type: s4
        if: has_rest_param != 0
      - id: has_receiver
        type: u1
      - id: reciever
        type: reciever
        if: has_receiver != 0
      - id: taint_table_length
        type: s8
      - id: taint_table
        type: taint_table
        size: taint_table_length
      - id: doc
        type: markdown
      - id: function_body_length
        type: s8
      - id: function_body
        size: function_body_length
  referenced_type:
    seq:
      - id: type_cp_index
        type: s4
  required_param:
    seq:
      - id: param_name_cp_index
        type: s4
      - id: flags
        type: s4
  reciever:
    seq:
      - id: kind
        type: s1
      - id: type_cp_index
        type: s4
      - id: name_cp_index
        type: s4
  taint_table:
    seq:
      - id: row_count
        type: s2
      - id: column_count
        type: s2
  position:
    seq:
      - id: s_line
        type: s4
      - id: e_line
        type: s4
      - id: s_col
        type: s4
      - id: e_col
        type: s4
      - id: source_file_cp_index
        type: s4
