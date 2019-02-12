## Ballerina Specification Conformance Tests

This module contains specification conformance tests for this implementation of the Ballerina specification.

### Compatibility

|  Specification/Language   |       Version         |
| :----------------------- | :------------------- |
| Ballerina Specification   | v0.99X:2019-MM-DD     |
| Ballerina Language        | v0.990.3              |


### Structure

```
.
├── S05_simple_values
│   └── tests
│       ├── deviations
│       │   ├── float.bal
│       │   └── nil.bal
│       ├── floating-point-types
│       │   ├── decimal.bal
│       │   └── float.bal
│       ├── boolean.bal
│       ├── int.bal
│       ├── nil.bal
│       └── string.bal
│
└── S05_structured_values
    └── tests
        ├── deviations
        │   ├── arrays.bal
        │   └── records.bal
        ├── lists
        │   ├── arrays
        │   │   ├── array_member_iteration.bal
        │   │   ├── array_member_order.bal
        │   │   └── array_member_reference_by_integer_index.bal
        │   └── tuples
        │       ├── tuple_covariance.bal
        │       ├── tuple_member_order.bal
        │       └── tuple_member_reference_by_integer_index.bal
        │       
        ├── mappings
        │   ├── maps
        │   │   ├── map_covariance.bal
        │   │   ├── map_field_unique_name.bal
        │   │   └── map_inherent_type_violation.bal
        │   └── records
        │       ├── record_covariance.bal
        │       ├── record_field_shape.bal
        │       └── record_field_types.bal
        ├── error.bal
        ├── structured_values.bal
        ├── table.bal
        └── xml.bal
```

- Tests for each main subsection in the specification should be introduced in the `tests` directory of a module that is named `SXX_section_name`, where
   - `XX` stands for the larger section number (e.g., 05 for "Values, types and variables", 06 for "Expressions", etc.). 
   - `section_name` stands for the subsection name (e.g., "simple_values", "structured_values", etc.). 
- The compliant tests for each section could be in top level `.bal` files in the tests directory itself (e.g., `boolean.bal`, `int.bal`) or in logically grouped subdirectories in the test directory (e.g., `.bal` files in the `arrays`, `maps`, etc. directories). 
- Tests written for **specification deviations** need to be included in a `deviations` directory within the tests directory.

### Deviation Tests

Tests for deviations could be of one of the following forms:
- A test written to pass according to the current implementation, which would fail once the deviation is rectified. The correct assertion (according to the specification) could also be written and commented out.
- A test written according to the expected behaviour, which currently fails at compilation due to deviations. Such a test would be commented out completely.

Each deviation test should be of the following structure:
```ballerina
// The nil value can also be written null, for
// compatibility with JSON; the use of null should be restricted to JSON-related contexts.
// TODO: Disallow the use of `null` with () type
// https://github.com/ballerina-platform/ballerina-lang/issues/13169
@test:Config {
    groups: ["deviation"]
}
function testNilBroken() {
   // test code
}
```

In other words, each deviation test 
- should contain the relevant content from the specification either as a generic comment or a specific comment 
- should have a "TODO: " specifying the deviation that needs to be fixed as a comment
- should mention the relevant GitHub issue as a comment
- should specify "deviation" as a test group to which the test belongs to
- should suffix "Broken" to the test name

#### Moving Deviation Tests

Once a deviation is fixed, the relevant tests in the `deviation` directory need to be moved to either the relevant `.bal` file or a new `.bal` file, with the necessary changes.

### Running the Test Suite

To run all the tests, navigate to the `ballerina-spec-conformance-tests` project and use the Ballerina `test` command.
```cmd
$ ballerina test --experimental
```

To skip the **deviation** tests, use the `--disable-groups` option with the Ballerina `test` command.
```cmd
$ ballerina test --experimental --disable-groups=deviation
```
