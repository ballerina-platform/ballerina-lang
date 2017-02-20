# Modularity & Versioning

Ballerina programs can be written in one or more files organized into packages.

## Packages

Files are declared to be in a package using the `package` declaration:

```
package PackageName;
```

If a file declares a package it must be the first line in the file. A `PackageName` is a collection of Identifiers separated by a `.` and must be stored in a directory hierarchy corresponding to the components of the package name. (This is the same as Java's approach for storing packages.)

A package defines a namespace. All symbols (e.g., service names, type names, and function names) declared in any file in the same package belong to that namespace. Every symbol has a qualified name consisting of its package name and its own top level name. Note that as of version 0.8 of Ballerina all symbols are public - in future there will likely be a `private` keyword added to control visibility of symbols.

To use symbols from other packages those packages must be `import`ed to the consuming file. When imported, the last part of the package name becomes the default prefix (or it can be aliased using `as Identifier`) for all symbols from the imported package. Thus, imported symbols are referred using qualified names as follows:

```
PackagePrefix:SymbolName
```

## Files

A Ballerina file is structured as follows:
```
[package PackageName;]
[import PackageName [version ImportVersionNumber] [as Identifier];]*

(ServiceDefinition |
 FunctionDefinition |
 ConnectorDefinition |
 TypeDefinition |
 TypeMapperDefinition |
 ConstantDefinition)+
```

## Versioning

Ballerina understands that dependent libraries may be versioned and offers the ability to indicate the desired version to import.

Every top-level symbol has a version string associated with it (with a default value of "1.0.0"). Packages may define their version number in the Major.Minor.Patch format. Package import statements may indicate the version that they are importing in the Major.Minor format; patch version levels are not relevant to package importers.

In the 0.8 release of Ballerina, the details of versioning are still under development and will combine the versioning concepts from Maven, OSGi, and Java to a model that is native to the language. As such, any version string other than "1.0" for an import version will be rejected by the compiler.

Note that there is currently no provision for declaring the version of a package.
