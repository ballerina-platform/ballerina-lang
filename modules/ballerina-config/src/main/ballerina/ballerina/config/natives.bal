package ballerina.config;

@Description { value:"Retrieve the specified global configuration value" }
@Param { value:"property: The configuration to be retrieved" }
@Return { value:"string: configuration value of property" }
public native function getGlobalValue(string property)(string);

@Description { value:"Retrieve the specified configuration value for the named instance" }
@Param { value:"instanceId: The ID of the instance" }
@Param { value:"property: The configuration to be retrieved" }
@Return { value:"string: configuration value of property" }
public native function getInstanceValue(string instanceId, string property)(string);
