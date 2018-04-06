//  Warning..!!!
//
//  ballerina.internal package defines Internal APIs exposed by Ballerina Virtual Machine (BVM).
//  They are subject to change in a undocumented or unsupported way, use with caution.
//
package ballerina.internal;

public type anyStruct {};

public type annotationData {
    string name,
    string pkgName,
    anyStruct value,
};

public native function getServiceAnnotations (typedesc serviceType) returns (annotationData[]);

public native function getResourceAnnotations (typedesc serviceType, string resourceName) returns (annotationData[]);

public native function getConnectorAnnotations (typedesc connectorType) returns (annotationData[]);

public native function getActionAnnotations (typedesc connectorType, string actionName) returns (annotationData[]);

public native function getStructAnnotations (typedesc structType) returns (annotationData[]);

public native function getStructFieldAnnotations (typedesc structType, string fieldName) returns (annotationData[]);

public native function getFunctionAnnotations (any functionPointer) returns (annotationData[]);
