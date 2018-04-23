
@Description { value:"Retrieve the specified global configuration value" }
@Param { value:"property: The configuration to be retrieved" }
@Return { value:"Configuration value of property" }
public native function getGlobalValue(string property)(string);

@Description { value:"Retrieve the specified configuration value for the named instance" }
@Param { value:"instanceId: The ID of the instance" }
@Param { value:"property: The configuration to be retrieved" }
@Return { value:"Configuration value of the property of the instance denoted by instanceId" }
public native function getInstanceValue(string instanceId, string property)(string);
