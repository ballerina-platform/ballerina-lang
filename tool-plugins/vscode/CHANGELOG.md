# Changelog

## 0.96.0 - 2018-02-01

### Added
- Support debugging ballerina programs
    - Stopping in debug points
    - Stepping over, stepping in, stepping out
    - Callstacks and variable values
- Updated language server to v0.96.0

## 0.95.8 - 2018-01-24

### Added
- Updated language server to v0.95.8 supporting ballerina v0.95.8

## 0.95.7 - 2018-01-18

### Added
- New features from language server v0.95.7
    - Go to definition
- Option to configure ballerina sdk path. See [how to](https://github.com/ballerinalang/plugin-vscode#configuring-the-ballerina-sdk-path).

### Fixed
- Hover and completions LS features not working in windows

## 0.95.6 - 2017-12-24

### Added
- Language Server Support
    - Enum and Struct support for hover provider.
    - Improvements to completion support
        - Enum completion support.
        - "next" keyword support.
        - New transaction syntax support.
    - Signature help improvements to aware of ballerina system libraries.
    
### Fixed
- Language Server Support
    - System library resolving.
    - Exception Handling.
    - Completion and suggestions Improvements with packaging awareness([#59](https://github.com/ballerinalang/language-server/issues/59)).

## 0.95.5 - 2017-12-18
### Added
- Language Server Support
    - Signature help
    - Hover provider support
    - Document symbol support
### Fixed
- Fixed the issue with endpoint snippet completion ([#20](https://github.com/ballerinalang/plugin-vscode/issues/20))

#### note 
- Here on plugin version will be synced with ballerina language version.

## 0.9.1 - 2017-12-08
### Fixed
- Moved npm dependencies to dependencies. Fixes the issue of dependencies not installing properly and language client not starting.

## 0.9.0 - 2017-12-08
### Added
- Language server support
    - Show completions
    - Show diagnostics on file change
- Updated keyword syntax highlighting
