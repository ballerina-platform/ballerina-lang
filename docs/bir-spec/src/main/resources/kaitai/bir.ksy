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
            'tag_enum::cp_entry_string': string_cp_info
            'tag_enum::cp_entry_package': package_cp_info
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
      - id: type_def_count
        type: s4
      - id: golbal_var_count
        type: s4
      - id: golbal_vars
        type: golbal_var
        repeat: expr
        repeat-expr: golbal_var_count
      - id: function_count
        type: s4
      - id: functions
        type: function
        repeat: expr
        repeat-expr: function_count
  golbal_var:
    seq:
      - id: name_cp_index
        type: s4
      - id: flags
        type: s4
      - id: doc
        type: markdown
      - id: type_cp_index
        type: s4
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
      - id: source_file_cp_index
        type: s4
      - id: name_cp_index
        type: s4
      - id: flags
        type: s4
      - id: type_cp_index
        type: s4
      - id: required_param_count
        type: s4
      - id: has_rest_param
        type: u1
      - id: has_receiver
        type: u1
      - id: taint_table_lenght
        type: s8
      - id: taint_table
        type: taint_table
        size: taint_table_lenght
      - id: doc
        type: markdown
  taint_table:
    seq:
      - id: row_count
        type: s2
      - id: column_count
        type: s2
       
        
