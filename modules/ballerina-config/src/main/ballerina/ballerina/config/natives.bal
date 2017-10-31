package ballerina.config;

import ballerina.doc;

@doc:Description { value:"Retrieve the specified global configuration value" }
@doc:Param { value:"property: The configuration to be retrieved" }
@doc:Return { value:"string: configuration value of property" }
public native function getGlobalValue(string property)(string);

@doc:Description { value:"Retrieve the specified configuration value for the named instance" }
@doc:Param { value:"instanceId: The ID of the instance" }
@doc:Param { value:"property: The configuration to be retrieved" }
@doc:Return { value:"string: configuration value of property" }
public native function getInstanceValue(string instanceId, string property)(string);
