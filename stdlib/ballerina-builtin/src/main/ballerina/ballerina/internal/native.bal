//  Warning..!!!
//
//  ballerina.internal package defines Internal APIs exposed by Ballerina Virtual Machine (BVM).
//  They are subject to change in a undocumented or unsupported way, use with caution.
//
package ballerina.internal;

public struct anyStruct {}

public struct annotationData {
    string name;
    string pkgName;
    anyStruct value;
}

public native function getServiceAnnotations (type serviceType) returns (annotationData[]);

public native function getResourceAnnotations (type serviceType, string resourceName) returns (annotationData[]);

public native function getConnectorAnnotations (type connectorType) returns (annotationData[]);

public native function getActionAnnotations (type connectorType, string actionName) returns (annotationData[]);

public native function getStructAnnotations (type structType) returns (annotationData[]);

public native function getStructFieldAnnotations (type structType, string fieldName) returns (annotationData[]);

public native function getFunctionAnnotations (any functionPointer) returns (annotationData[]);
