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
      - id: shape_length
        type: s4
      - id: shape
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
            'type_tag_enum::type_tag_array': type_array
            'type_tag_enum::type_tag_error': type_error
            'type_tag_enum::type_tag_finite': type_finite
            'type_tag_enum::type_tag_invokable': type_invokable
            'type_tag_enum::type_tag_map': type_map
            'type_tag_enum::type_tag_stream': type_stream
            'type_tag_enum::type_tag_typedesc': type_typedesc
            'type_tag_enum::type_tag_parameterized_type': type_parameterized
            'type_tag_enum::type_tag_future': type_future
            'type_tag_enum::type_tag_object_or_service': type_object_or_service
            'type_tag_enum::type_tag_tuple': type_tuple
            'type_tag_enum::type_tag_union': type_union
            'type_tag_enum::type_tag_intersection': type_intersection
            'type_tag_enum::type_tag_record': type_record
            'type_tag_enum::type_tag_xml': type_xml
            'type_tag_enum::type_tag_table': type_table
    instances:
      name_as_str:
        value: _root.constant_pool.constant_pool_entries[name_index].cp_info.as<string_cp_info>.value
  type_array:
    seq:
      - id: state
        type: s1
      - id: size
        type: s4
      - id: element_type_index
        type: s4
  type_error:
    seq:
      - id: pkg_id_cp_index
        type: s4
      - id: error_type_name_cp_index
        type: s4
      - id: detail_type_cp_index
        type: s4
      - id: type_ids
        type: type_id
  type_id:
    seq:
      - id: primary_type_id_count
        type: s4
      - id: primary_type_id
        type: type_id_set
        repeat: expr
        repeat-expr: primary_type_id_count
      - id: secondary_type_id_count
        type: s4
      - id: secondary_type_id
        type: type_id_set
        repeat: expr
        repeat-expr: secondary_type_id_count
  type_id_set:
    seq:
      - id: pkg_id_cp_index
        type: s4
      - id: type_id_name_cp_index
        type: s4
      - id: is_public_id
        type: u1
  type_finite:
    seq:
      - id: name_cp_index
        type: s4
      - id: flags
        type: s4
      - id: value_space_size
        type: s4
      - id: finite_values
        type: finite_value
        repeat: expr
        repeat-expr: value_space_size
  finite_value:
    seq:
      - id : type_cp_index
        type: s4
      - id: value_length
        type: s4
      - id: value
        size: value_length
  type_invokable:
    seq:
      - id: param_types_count
        type: s4
      - id: param_type_cp_index
        type: s4
        repeat: expr
        repeat-expr: param_types_count
      - id: has_rest_type
        type: u1
      - id: rest_type_cp_index
        type: s4
        if: has_rest_type == 1
      - id: return_type_cp_index
        type: s4
  type_map:
    seq:
      - id: constraint_type_cp_index
        type: s4
  type_stream:
    seq:
      - id: constraint_type_cp_index
        type: s4
      - id: has_error_type
        type: u1
      - id: error_type_cp_index
        type: s4
        if: has_error_type == 1
  type_typedesc:
    seq:
      - id: constraint_type_cp_index
        type: s4
  type_parameterized:
    seq:
      - id: param_value_type_cp_index
        type: s4
  type_future:
    seq:
      - id: constraint_type_cp_index
        type: s4
  type_object_or_service:
    seq:
      - id: is_object_type
        type: s1
      - id: pkd_id_cp_index
        type: s4
      - id: name_cp_index
        type: s4
      - id: is_abstract
        type: u1
      - id: is_client
        type: u1
      - id: object_fields_count
        type: s4
      - id: object_fields
        type: object_field
        repeat: expr
        repeat-expr: object_fields_count
      - id: has_generated_init_function
        type: s1
      - id: generated_init_function
        type: object_attached_function
        if: has_generated_init_function == 1
      - id: has_init_function
        type: s1
      - id: init_function
        type: object_attached_function
        if: has_init_function == 1
      - id: object_attached_functions_count
        type: s4
      - id: object_attached_functions
        type: object_attached_function
        repeat: expr
        repeat-expr: object_attached_functions_count
      - id: type_ids
        type: type_id
  object_field:
    seq:
      - id: name_cp_index
        type: s4
      - id: flags
        type: s4
      - id: doc
        type: markdown
      - id: type_cp_index
        type: s4
  object_attached_function:
    seq:
      - id: name_cp_index
        type: s4
      - id: flags
        type: s4
      - id: type_cp_index
        type: s4
  type_union:
    seq:
      - id: member_types_count
        type: s4
      - id: member_type_cp_index
        type: s4
        repeat: expr
        repeat-expr: member_types_count
  type_tuple:
    seq:
      - id: tuple_types_count
        type: s4
      - id: tuple_type_cp_index
        type: s4
        repeat: expr
        repeat-expr: tuple_types_count
      - id: has_rest_type
        type: u1
      - id: rest_type_cp_index
        type: s4
        if: has_rest_type == 1
  type_intersection:
    seq:
      - id: constituent_types_count
        type: s4
      - id: constituent_type_cp_index
        type: s4
        repeat: expr
        repeat-expr: constituent_types_count
      - id: effective_type_count
        type: s4
  type_xml:
    seq:
      - id: constraint_type_cp_index
        type: s4
  type_table:
    seq:
      - id: constraint_type_cp_index
        type: s4
      - id: has_field_name_list
        type: u1
      - id: field_name_list
        type: table_field_name_list
        if: has_field_name_list == 1
      - id: has_key_constraint_type
        type: u1
      - id : key_constraint_type_cp_index
        type: s4
        if: has_key_constraint_type == 1
  table_field_name_list:
    seq:
      - id: size
        type: s4
      - id: field_name_cp_index
        type: s4
        repeat: expr
        repeat-expr: size
  type_record:
    seq:
      - id: pkd_id_cp_index
        type: s4
      - id: name_cp_index
        type: s4
      - id: is_sealed
        type: u1
      - id: rest_field_type_cp_index
        type: s4
      - id: record_fields_count
        type: s4
      - id: record_fields
        type: record_field
        repeat: expr
        repeat-expr: record_fields_count
      - id: has_init_function
        type: s1
      - id: record_init_function
        type: record_init_function
        if: has_init_function == 1
  record_field:
    seq:
      - id: name_cp_index
        type: s4
      - id: flags
        type: s4
      - id: doc
        type: markdown
      - id: type_cp_index
        type: s4
  record_init_function:
    seq:
      - id: name_cp_index
        type: s4
      - id: flags
        type: s4
      - id: type_cp_index
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
      - id: annotations
        type: annotation
        repeat: expr
        repeat-expr: annotations_size
  golbal_var:
    seq:
      - id: kind
        type: s1
      - id: name_cp_index
        type: s4
      - id: flags
        type: s4
      - id: origin
        type: s1
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
      - id: origin
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
  annotation:
    seq:
      - id: name_cp_index
        type: s4
      - id: flags
        type: s4
      - id: origin
        type: s1
      - id: position
        type: position
      - id: attach_points_count
        type: s4
      - id: attach_points
        type: attach_point
        repeat: expr
        repeat-expr: attach_points_count
      - id: annotation_type_cp_index
        type: s4
      - id: doc
        type: markdown
  attach_point:
    seq:
      - id: point_name_cp_index
        type: s4
      - id: is_source
        type: u1
  constant:
    seq:
      - id: name_cp_index
        type: s4
      - id: flags
        type: s4
      - id: origin
        type: s1
      - id: position
        type: position
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
            'type_tag_enum::type_tag_signed32_int': int_constant_info
            'type_tag_enum::type_tag_signed16_int': int_constant_info
            'type_tag_enum::type_tag_signed8_int': int_constant_info
            'type_tag_enum::type_tag_unsigned32_int': int_constant_info
            'type_tag_enum::type_tag_unsigned16_int': int_constant_info
            'type_tag_enum::type_tag_unsigned8_int': int_constant_info
            'type_tag_enum::type_tag_byte': byte_constant_info
            'type_tag_enum::type_tag_float': float_constant_info
            'type_tag_enum::type_tag_string': string_constant_info
            'type_tag_enum::type_tag_char_string': string_constant_info
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
        type: u1
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
        type: u1
      - id: markdown_content
        type: markdown_content
        if: has_doc == 1
  markdown_content:
    seq:
      - id: description_cp_index
        type: s4
      - id: return_value_description_cp_index
        type: s4
      - id: parameters_count
        type: s4
      - id: parameters
        type: markdown_parameter
        repeat: expr
        repeat-expr: parameters_count
  markdown_parameter:
    seq:
      - id: name_cp_index
        type: s4
      - id: description_cp_index
        type: s4
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
      - id: origin
        type: s1
      - id: type_cp_index
        type: s4
      - id: annotation_attachments_content
        type: annotation_attachments_content
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
      - id: doc
        type: markdown
      - id: dependent_global_var_length
        type: s4
      - id: dependent_global_var_cp_entry
        type: s4
        repeat: expr
        repeat-expr: dependent_global_var_length
      - id: scope_table_length
        type: s8
      - id: scope_entry_count
        type: s4
      - id: scope_entries
        type: scope_entry
        repeat: expr
        repeat-expr: scope_entry_count
      - id: function_body_length
        type: s8
      - id: function_body
        type: function_body
        size: function_body_length
  annotation_attachments_content:
    seq:
      - id: annotation_attachments_content_length
        type: s8
      - id: attachments_count
        type: s4
      - id: annotation_attachments
        type: annotation_attachment
        repeat: expr
        repeat-expr: attachments_count
  annotation_attachment:
    seq:
      - id: package_id_cp_index
        type: s4
      - id: position
        type: position
      - id: tag_reference_cp_index
        type: s4
      - id: attach_values_count
        type: s4
      - id: attache_values
        type: attach_value
        repeat: expr
        repeat-expr: attach_values_count
  attach_value:
    seq:
      - id: attach_value_type_cp_index
        type: s4
      - id: attach_value_value_info
        type:
          switch-on: attach_type.shape.type_tag
          cases:
            'type_tag_enum::type_tag_array': attach_value_array
            'type_tag_enum::type_tag_map': attach_value_map
            'type_tag_enum::type_tag_record': attach_value_map
            'type_tag_enum::type_tag_int': int_constant_info
            'type_tag_enum::type_tag_signed32_int': int_constant_info
            'type_tag_enum::type_tag_signed16_int': int_constant_info
            'type_tag_enum::type_tag_signed8_int': int_constant_info
            'type_tag_enum::type_tag_unsigned32_int': int_constant_info
            'type_tag_enum::type_tag_unsigned16_int': int_constant_info
            'type_tag_enum::type_tag_unsigned8_int': int_constant_info
            'type_tag_enum::type_tag_byte': byte_constant_info
            'type_tag_enum::type_tag_float': float_constant_info
            'type_tag_enum::type_tag_string': string_constant_info
            'type_tag_enum::type_tag_char_string': string_constant_info
            'type_tag_enum::type_tag_decimal': decimal_constant_info
            'type_tag_enum::type_tag_boolean': boolean_constant_info
            'type_tag_enum::type_tag_nil': nil_constant_info
    instances:
      attach_type:
        value: _root.constant_pool.constant_pool_entries[attach_value_type_cp_index].cp_info.as<shape_cp_info>
  attach_value_array:
    seq:
      - id: array_values_count
        type: s4
      - id: array_values
        type: attach_value
        repeat: expr
        repeat-expr: array_values_count
  attach_value_map:
    seq:
      - id: map_entries_count
        type: s4
      - id: map_entries
        type: attach_value_map_entry
        repeat: expr
        repeat-expr: map_entries_count
  attach_value_map_entry:
    seq:
      - id: key_name_cp_index
        type: s4
      - id: key_value_info
        type: attach_value
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
      - id: taint_table_size
        type: s4
      - id: taint_table_entries
        type: taint_table_entry
        repeat: expr
        repeat-expr: taint_table_size
  taint_table_entry:
    seq:
      - id: param_index
        type: s2
      - id: taint_record_size
        type: s4
      - id: taint_status
        type: s1
        repeat: expr
        repeat-expr: taint_record_size
  scope_entry:
    seq:
      - id: current_scope_index
        type: s4
      - id: instruction_offset
        type: s4
      - id: has_parent
        type: u1
      - id: parent_scope_index
        type: s4
        if: has_parent == 1
  function_body:
    seq:
      - id: args_count
        type: s4
      - id: has_return_var
        type: u1
      - id: return_var
        type: return_var
        if: has_return_var == 1
      - id: default_parameter_count
        type: s4
      - id: default_parameters
        type: default_parameter
        repeat: expr
        repeat-expr: default_parameter_count
      - id: local_variables_count
        type: s4
      - id: local_variables
        type: local_variable
        repeat: expr
        repeat-expr: local_variables_count
      - id: default_parameter_basic_blocks_info
        type: basic_blocks_info
        repeat: expr
        repeat-expr: default_parameter_count
      - id: function_basic_blocks_info
        type: basic_blocks_info
      - id: error_table
        type: error_table
      - id: worker_channel_info
        type: worker_channel
  return_var:
    seq:
      - id: kind
        type: s1
      - id: type_cp_index
        type: s4
      - id: name_cp_index
        type: s4
  default_parameter:
    seq:
      - id: kind
        type: s1
      - id: type_cp_index
        type: s4
      - id: name_cp_index
        type: s4
      - id: meta_var_name_cp_index
        type: s4
        if: kind == 2
      - id: has_default_expr
        type: u1
  local_variable:
    seq:
      - id: kind
        type: s1
      - id: type_cp_index
        type: s4
      - id: name_cp_index
        type: s4
      - id: meta_var_name_cp_index
        type: s4
        if: kind == 2
      - id: enclosing_basic_block_id
        type: enclosing_basic_block_id
        if: kind == 1
  error_table:
    seq:
      - id: error_entries_count
        type: s4
      - id: error_entries
        type: error_entry
        repeat: expr
        repeat-expr: error_entries_count
  error_entry:
    seq:
      - id: trap_bb_id_cp_index
        type: s4
      - id: end_bb_id_cp_index
        type: s4
      - id: error_operand
        type: operand
      - id: target_bb_id_cp_index
        type: s4
  worker_channel:
    seq:
      - id: channels_length
        type: s4
      - id: worker_channel_details
        type: worker_channel_detail
        repeat: expr
        repeat-expr: channels_length
  worker_channel_detail:
    seq:
      - id: name_cp_index
        type: s4
      - id: is_channel_in_same_strand
        type: u1
      - id: is_send
        type: u1
  enclosing_basic_block_id:
    seq:
      - id: meta_var_name_cp_index
        type: s4
      - id: end_bb_id_cp_index
        type: s4
      - id: start_bb_id_cp_index
        type: s4
      - id: instruction_offset
        type: s4
  position:
    seq:
      - id: source_file_cp_index
        type: s4
      - id: s_line
        type: s4
      - id: s_col
        type: s4
      - id: e_line
        type: s4
      - id: e_col
        type: s4
  basic_blocks_info:
    seq:
      - id: basic_blocks_count
        type: s4
      - id: basic_blocks
        type: basic_block
        repeat: expr
        repeat-expr: basic_blocks_count
  basic_block:
    seq:
      - id: name_cp_index
        type: s4
      - id: instructions_count
        type: s4
      - id: instructions
        type: instruction
        repeat: expr
        repeat-expr: instructions_count
  instruction:
    seq:
      - id: position
        type: position
      - id: instruction_kind
        type: s1
        enum: instruction_kind_enum
      - id: instruction_structure
        type:
          switch-on: instruction_kind
          cases:
            'instruction_kind_enum::instruction_kind_goto': instruction_goto
            'instruction_kind_enum::instruction_kind_call': instruction_call
            'instruction_kind_enum::instruction_kind_branch': instruction_branch
            'instruction_kind_enum::instruction_kind_return': instruction_return
            'instruction_kind_enum::instruction_kind_async_call': instruction_async_call
            'instruction_kind_enum::instruction_kind_wait': instruction_wait
            'instruction_kind_enum::instruction_kind_fp_call': instruction_fp_call
            'instruction_kind_enum::instruction_kind_wk_receive': instruction_wk_receive
            'instruction_kind_enum::instruction_kind_wk_send': instruction_wk_send
            'instruction_kind_enum::instruction_kind_flush': instruction_flush
            'instruction_kind_enum::instruction_kind_lock': instruction_lock
            'instruction_kind_enum::instruction_kind_field_lock': instruction_field_lock
            'instruction_kind_enum::instruction_kind_unlock': instruction_unlock
            'instruction_kind_enum::instruction_kind_wait_all': instruction_wait_all
            'instruction_kind_enum::instruction_kind_move': instruction_move
            'instruction_kind_enum::instruction_kind_const_load': instruction_const_load
            'instruction_kind_enum::instruction_kind_new_structure': instruction_new_structure
            'instruction_kind_enum::instruction_kind_map_store': instruction_map_store
            'instruction_kind_enum::instruction_kind_map_load': instruction_map_load
            'instruction_kind_enum::instruction_kind_new_array': instruction_new_array
            'instruction_kind_enum::instruction_kind_array_store': instruction_array_store
            'instruction_kind_enum::instruction_kind_array_load': instruction_array_load
            'instruction_kind_enum::instruction_kind_new_error': instruction_new_error
            'instruction_kind_enum::instruction_kind_type_cast': instruction_type_cast
            'instruction_kind_enum::instruction_kind_is_like': instruction_is_like
            'instruction_kind_enum::instruction_kind_type_test': instruction_type_test
            'instruction_kind_enum::instruction_kind_new_instance': instruction_new_instance
            'instruction_kind_enum::instruction_kind_object_store': instruction_object_store
            'instruction_kind_enum::instruction_kind_object_load': instruction_object_load
            'instruction_kind_enum::instruction_kind_panic': instruction_panic
            'instruction_kind_enum::instruction_kind_fp_load': instruction_fp_load
            'instruction_kind_enum::instruction_kind_string_load': instruction_string_load
            'instruction_kind_enum::instruction_kind_new_xml_element': instruction_new_xml_element
            'instruction_kind_enum::instruction_kind_new_xml_text': instruction_new_xml_text
            'instruction_kind_enum::instruction_kind_new_xml_comment': instruction_new_xml_comment
            'instruction_kind_enum::instruction_kind_new_xml_pi': instruction_new_xml_process_ins
            'instruction_kind_enum::instruction_kind_new_xml_qname': instruction_new_xml_qname
            'instruction_kind_enum::instruction_kind_new_string_xml_qname': instruction_new_string_xml_qname
            'instruction_kind_enum::instruction_kind_xml_seq_store': instruction_xml_seq_store
            'instruction_kind_enum::instruction_kind_xml_seq_load': instruction_xml_seq_load
            'instruction_kind_enum::instruction_kind_xml_load': instruction_xml_load
            'instruction_kind_enum::instruction_kind_xml_load_all': instruction_xml_load_all
            'instruction_kind_enum::instruction_kind_xml_attribute_load': instruction_xml_attribute_load
            'instruction_kind_enum::instruction_kind_xml_attribute_store': instruction_xml_attribute_store
            'instruction_kind_enum::instruction_kind_new_table': instruction_new_table
            'instruction_kind_enum::instruction_kind_new_typedesc': instruction_new_typedesc
            'instruction_kind_enum::instruction_kind_table_store': instruction_table_store
            'instruction_kind_enum::instruction_kind_table_load': instruction_table_load
            'instruction_kind_enum::instruction_kind_add': instruction_binary_operation
            'instruction_kind_enum::instruction_kind_sub': instruction_binary_operation
            'instruction_kind_enum::instruction_kind_mul': instruction_binary_operation
            'instruction_kind_enum::instruction_kind_div': instruction_binary_operation
            'instruction_kind_enum::instruction_kind_mod': instruction_binary_operation
            'instruction_kind_enum::instruction_kind_equal': instruction_binary_operation
            'instruction_kind_enum::instruction_kind_not_equal': instruction_binary_operation
            'instruction_kind_enum::instruction_kind_greater_than': instruction_binary_operation
            'instruction_kind_enum::instruction_kind_greater_equal': instruction_binary_operation
            'instruction_kind_enum::instruction_kind_less_than': instruction_binary_operation
            'instruction_kind_enum::instruction_kind_less_equal': instruction_binary_operation
            'instruction_kind_enum::instruction_kind_and': instruction_binary_operation
            'instruction_kind_enum::instruction_kind_or': instruction_binary_operation
            'instruction_kind_enum::instruction_kind_ref_equal': instruction_binary_operation
            'instruction_kind_enum::instruction_kind_ref_not_equal': instruction_binary_operation
            'instruction_kind_enum::instruction_kind_closed_range': instruction_binary_operation
            'instruction_kind_enum::instruction_kind_half_open_range': instruction_binary_operation
            'instruction_kind_enum::instruction_kind_annot_access': instruction_binary_operation
            'instruction_kind_enum::instruction_kind_bitwise_and': instruction_binary_operation
            'instruction_kind_enum::instruction_kind_bitwise_or': instruction_binary_operation
            'instruction_kind_enum::instruction_kind_bitwise_xor': instruction_binary_operation
            'instruction_kind_enum::instruction_kind_bitwise_left_shift': instruction_binary_operation
            'instruction_kind_enum::instruction_kind_bitwise_right_shift': instruction_binary_operation
            'instruction_kind_enum::instruction_kind_bitwise_unsigned_right_shift': instruction_binary_operation
            'instruction_kind_enum::instruction_kind_typeof': instruction_unary_operation
            'instruction_kind_enum::instruction_kind_not': instruction_unary_operation
            'instruction_kind_enum::instruction_kind_negate': instruction_unary_operation
    enums:
      instruction_kind_enum:
        1: instruction_kind_goto
        2: instruction_kind_call
        3: instruction_kind_branch
        4: instruction_kind_return
        5: instruction_kind_async_call
        6: instruction_kind_wait
        7: instruction_kind_fp_call
        8: instruction_kind_wk_receive
        9: instruction_kind_wk_send
        10: instruction_kind_flush
        11: instruction_kind_lock
        12: instruction_kind_field_lock
        13: instruction_kind_unlock
        14: instruction_kind_wait_all
        20: instruction_kind_move
        21: instruction_kind_const_load
        22: instruction_kind_new_structure
        23: instruction_kind_map_store
        24: instruction_kind_map_load
        25: instruction_kind_new_array
        26: instruction_kind_array_store
        27: instruction_kind_array_load
        28: instruction_kind_new_error
        29: instruction_kind_type_cast
        30: instruction_kind_is_like
        31: instruction_kind_type_test
        32: instruction_kind_new_instance
        33: instruction_kind_object_store
        34: instruction_kind_object_load
        35: instruction_kind_panic
        36: instruction_kind_fp_load
        37: instruction_kind_string_load
        38: instruction_kind_new_xml_element
        39: instruction_kind_new_xml_text
        40: instruction_kind_new_xml_comment
        41: instruction_kind_new_xml_pi
        42: instruction_kind_new_xml_seq
        43: instruction_kind_new_xml_qname
        44: instruction_kind_new_string_xml_qname
        45: instruction_kind_xml_seq_store
        46: instruction_kind_xml_seq_load
        47: instruction_kind_xml_load
        48: instruction_kind_xml_load_all
        49: instruction_kind_xml_attribute_load
        50: instruction_kind_xml_attribute_store
        51: instruction_kind_new_table
        52: instruction_kind_new_typedesc
        53: instruction_kind_new_stream
        54: instruction_kind_table_store
        55: instruction_kind_table_load
        61: instruction_kind_add
        62: instruction_kind_sub
        63: instruction_kind_mul
        64: instruction_kind_div
        65: instruction_kind_mod
        66: instruction_kind_equal
        67: instruction_kind_not_equal
        68: instruction_kind_greater_than
        69: instruction_kind_greater_equal
        70: instruction_kind_less_than
        71: instruction_kind_less_equal
        72: instruction_kind_and
        73: instruction_kind_or
        74: instruction_kind_ref_equal
        75: instruction_kind_ref_not_equal
        76: instruction_kind_closed_range
        77: instruction_kind_half_open_range
        78: instruction_kind_annot_access
        80: instruction_kind_typeof
        81: instruction_kind_not
        82: instruction_kind_negate
        83: instruction_kind_bitwise_and
        84: instruction_kind_bitwise_or
        85: instruction_kind_bitwise_xor
        86: instruction_kind_bitwise_left_shift
        87: instruction_kind_bitwise_right_shift
        88: instruction_kind_bitwise_unsigned_right_shift
        128: instruction_kind_platform
  instruction_const_load:
    seq:
      - id: type_cp_index
        type: s4
      - id: lhs_operand
        type: operand
      - id: constant_value_info
        type:
          switch-on: type.shape.type_tag
          cases:
            'type_tag_enum::type_tag_int': int_constant_info
            'type_tag_enum::type_tag_signed32_int': int_constant_info
            'type_tag_enum::type_tag_signed16_int': int_constant_info
            'type_tag_enum::type_tag_signed8_int': int_constant_info
            'type_tag_enum::type_tag_unsigned32_int': int_constant_info
            'type_tag_enum::type_tag_unsigned16_int': int_constant_info
            'type_tag_enum::type_tag_unsigned8_int': int_constant_info
            'type_tag_enum::type_tag_byte': byte_constant_info
            'type_tag_enum::type_tag_float': float_constant_info
            'type_tag_enum::type_tag_string': string_constant_info
            'type_tag_enum::type_tag_char_string': string_constant_info
            'type_tag_enum::type_tag_decimal': decimal_constant_info
            'type_tag_enum::type_tag_boolean': boolean_constant_info
            'type_tag_enum::type_tag_nil': nil_constant_info
    instances:
      type:
        value: _root.constant_pool.constant_pool_entries[type_cp_index].cp_info.as<shape_cp_info>
  instruction_goto:
    seq:
      - id: target_bb_id_name_cp_index
        type: s4
  instruction_move:
    seq:
      - id: rhs_operand
        type: operand
      - id: lhs_operand
        type: operand
  instruction_call:
    seq:
      - id: call_instruction_info
        type: call_instruction_info
      - id: then_bb_id_name_cp_index
        type: s4
  call_instruction_info:
    seq:
      - id: is_virtual
        type: u1
      - id: package_index
        type: s4
      - id: call_name_cp_index
        type: s4
      - id: arguments_count
        type: s4
      - id: arguments
        type: operand
        repeat: expr
        repeat-expr: arguments_count
      - id: has_lhs_operand
        type: s1
      - id: lhs_operand
        type: operand
        if: has_lhs_operand != 0
  instruction_return:
    seq:
      - id: no_value
        size: 0
  instruction_new_typedesc:
    seq:
      - id: lhs_operand
        type: operand
      - id: type_cp_index
        type: s4
  instruction_new_structure:
    seq:
      - id: rhs_operand
        type: operand
      - id: lhs_operand
        type: operand
  instruction_type_cast:
    seq:
      - id: lhs_operand
        type: operand
      - id: rhs_operand
        type: operand
      - id: type_cp_index
        type: s4
      - id: is_check_types
        type: u1
  instruction_new_array:
    seq:
      - id: type_cp_index
        type: s4
      - id: lhs_operand
        type: operand
      - id: size_operand
        type: operand
  instruction_branch:
    seq:
      - id: branch_operand
        type: operand
      - id: true_bb_id_name_cp_index
        type: s4
      - id: false_bb_id_name_cp_index
        type: s4
  instruction_async_call:
    seq:
      - id: call_instruction_info
        type: call_instruction_info
      - id: annotation_attachments_content
        type: annotation_attachments_content
      - id: then_bb_id_name_cp_index
        type: s4
  instruction_wait:
    seq:
      - id: wait_expressions_count
        type: s4
      - id: wait_expressions
        type: operand
        repeat: expr
        repeat-expr: wait_expressions_count
      - id: lhs_operand
        type: operand
      - id: then_bb_id_name_cp_index
        type: s4
  instruction_fp_call:
    seq:
      - id: fp_operand
        type: operand
      - id: fp_arguments_count
        type: s4
      - id: fp_arguments
        type: operand
        repeat: expr
        repeat-expr: fp_arguments_count
      - id: has_lhs_operand
        type: s1
      - id: lhs_operand
        type: operand
        if: has_lhs_operand == 1
      - id: is_asynch
        type: u1
      - id: then_bb_id_name_cp_index
        type: s4
  instruction_wk_receive:
    seq:
      - id: worker_name_cp_index
        type: s4
      - id: lhs_operand
        type: operand
      - id: is_same_strand
        type: u1
      - id: then_bb_id_name_cp_index
        type: s4
  instruction_wk_send:
    seq:
      - id: channel_name_cp_index
        type: s4
      - id: worker_data_operand
        type: operand
      - id: is_same_strand
        type: u1
      - id: is_synch
        type: u1
      - id: lhs_operand
        type: operand
        if: is_synch == 1
      - id: then_bb_id_name_cp_index
        type: s4
  instruction_flush:
    seq:
      - id: worker_channel_detail
        type: worker_channel
      - id: lhs_operand
        type: operand
      - id: then_bb_id_name_cp_index
        type: s4
  instruction_lock:
    seq:
      - id: lock_bb_id_name_cp_index
        type: s4
  instruction_unlock:
    seq:
      - id: unlock_bb_id_name_cp_index
        type: s4
  instruction_field_lock:
    seq:
      - id: lock_var_name_cp_index
        type: s4
      - id: field_name_cp_index
        type: s4
      - id: lock_bb_id_name_cp_index
        type: s4
  instruction_wait_all:
    seq:
      - id: lhs_operand
        type: operand
      - id: key_names_count
        type: s4
      - id: key_name_cp_index
        type: s4
        repeat: expr
        repeat-expr: key_names_count
      - id: value_expressions_count
        type: s4
      - id: value_expression
        type: operand
        repeat: expr
        repeat-expr: value_expressions_count
      - id: then_bb_id_name_cp_index
        type: s4
  instruction_map_store:
    seq:
      - id: map_store
        type: index_access
  instruction_array_store:
    seq:
      - id: array_store
        type: index_access
  instruction_map_load:
    seq:
      - id: is_optional_field_access
        type: u1
      - id: is_filling_read
        type: u1
      - id: map_load
        type: index_access
  instruction_array_load:
    seq:
      - id: is_optional_field_access
        type: u1
      - id: is_filling_read
        type: u1
      - id: array_load
        type: index_access
  index_access:
    seq:
      - id: lhs_operand
        type: operand
      - id: key_operand
        type: operand
      - id: rhs_operand
        type: operand
  instruction_new_error:
    seq:
      - id: error_type_cp_index
        type: s4
      - id: lhs_operand
        type: operand
      - id: message_operand
        type: operand
      - id: cause_operand
        type: operand
      - id: detail_operand
        type: operand
  instruction_is_like:
    seq:
      - id: type_cp_index
        type: s4
      - id: lhs_operand
        type: operand
      - id: rhs_operand
        type: operand
  instruction_type_test:
    seq:
      - id: type_cp_index
        type: s4
      - id: lhs_operand
        type: operand
      - id: rhs_operand
        type: operand
  instruction_new_instance:
    seq:
      - id: is_external_definition
        type: u1
      - id: external_type_defintion_info
        type: external_type_defintion_info
        if: is_external_definition == 1
      - id: definition_index
        type: s4
        if: is_external_definition == 0
      - id: lhs_operand
        type: operand
  external_type_defintion_info:
    seq:
      - id: external_pkg_id_cp_index
        type: s4
      - id: object_name_cp_index
        type: s4
  instruction_object_store:
    seq:
      - id: object_store
        type: index_access
  instruction_object_load:
    seq:
      - id: object_load
        type: index_access
  instruction_panic:
    seq:
      - id: error_operand
        type: operand
  instruction_fp_load:
    seq:
      - id: lhs_operand
        type: operand
      - id: pkg_index_cp_index
        type: s4
      - id: function_name_cp_index
        type: s4
      - id: return_type_cp_index
        type: s4
      - id: closure_maps_size
        type: s4
      - id: closure_map_operand
        type: operand
        repeat: expr
        repeat-expr: closure_maps_size
      - id: fp_load_function_params_count
        type: s4
      - id: fp_load_function_params
        type: fp_load_function_param
        repeat: expr
        repeat-expr: fp_load_function_params_count
  fp_load_function_param:
    seq:
      - id: kind
        type: s1
      - id: type_cp_index
        type: s4
      - id: name_cp_index
        type: s4
  instruction_string_load:
    seq:
      - id: string_load
        type: index_access
  instruction_new_xml_element:
    seq:
      - id: lhs_operand
        type: operand
      - id: start_tag_operand
        type: operand
      - id: default_ns_uri_operand
        type: operand
  instruction_new_xml_text:
    seq:
      - id: lhs_operand
        type: operand
      - id: text_operand
        type: operand
  instruction_new_xml_qname:
    seq:
      - id: lhs_operand
        type: operand
      - id: local_name_operand
        type: operand
      - id: ns_uri_operand
        type: operand
      - id: prefix_operand
        type: operand
  instruction_new_xml_comment:
    seq:
      - id: lhs_operand
        type: operand
      - id: text_operand
        type: operand
  instruction_new_xml_process_ins:
    seq:
      - id: lhs_operand
        type: operand
      - id: data_operand
        type: operand
      - id: target_operand
        type: operand
  instruction_new_string_xml_qname:
    seq:
      - id: lhs_operand
        type: operand
      - id: string_qname_operand
        type: operand
  instruction_xml_seq_store:
    seq:
      - id: xml_seq_store
        type: xml_access
  instruction_xml_seq_load:
    seq:
      - id: xml_seq_load
        type: index_access
  instruction_xml_load:
    seq:
      - id: xml_load
        type: index_access
  instruction_xml_load_all:
    seq:
      - id: xml_load_all
        type: xml_access
  instruction_xml_attribute_load:
    seq:
      - id: xml_attribute_load
        type: index_access
  instruction_xml_attribute_store:
    seq:
      - id: xml_attribute_store
        type: index_access
  xml_access:
    seq:
      - id: lhs_operand
        type: operand
      - id: rhs_operand
        type: operand
  instruction_new_table:
    seq:
      - id: type_cp_index
        type: s4
      - id: lhs_operand
        type: operand
      - id: key_column_operand
        type: operand
      - id: data_operand
        type: operand
  instruction_table_store:
    seq:
      - id: table_store
        type: index_access
  instruction_table_load:
    seq:
      - id: table_load
        type: index_access
  instruction_binary_operation:
    seq:
      - id: rhs_operand_one
        type: operand
      - id: rhs_operand_two
        type: operand
      - id: lhs_operand
        type: operand
  instruction_unary_operation:
    seq:
      - id: rhs_operand
        type: operand
      - id: lhs_operand
        type: operand
  operand:
    seq:
      - id: ignored_variable
        type: u1
      - id: ignored_type_cp_index
        type: s4
        if: ignored_variable == 1
      - id: variable
        type: variable
        if: ignored_variable == 0
  variable:
    seq:
      - id: kind
        type: s1
      - id: scope
        type: s1
      - id: variable_dcl_name_cp_index
        type: s4
      - id: global_or_constant_variable
        type: global_variable
        if: kind == 5 or kind == 7
  global_variable:
    seq:
      - id: package_index
        type: s4
      - id: type_cp_index
        type: s4
enums:
  type_tag_enum:
    1: type_tag_int
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
    33: type_tag_object_or_service
    34: type_tag_byte_array
    35: type_tag_function_pointer
    36: type_tag_handle
    37: type_tag_readonly
    38: type_tag_signed32_int
    39: type_tag_signed16_int
    40: type_tag_signed8_int
    41: type_tag_unsigned32_int
    42: type_tag_unsigned16_int
    43: type_tag_unsigned8_int
    44: type_tag_char_string
    45: type_tag_xml_element
    46: type_tag_xml_pi
    47: type_tag_xml_comment
    48: type_tag_xml_text
    49: type_tag_never
    50: type_tag_null_set
    51: type_tag_parameterized_type
