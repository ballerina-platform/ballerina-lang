## Package overview

This package provides the common record types and constants required for other data management packages such as jdbc, mysql, h2. 

### PoolOptions 

The PoolOptions type the properties which are used to configure DB connection pool. This is used with jdbc, mysql and h2 endpoints to configure the connection pool associated with the endpoint.

### SQLType

The SQLType represents the SQL data type of a given parameter. When using a parameter the correct sql type which matches with the actual database table column type should be used. Otherwise data loss can be occured.

### Direction

The Direction type represents the direction of the parameter. IN parameters are used to send values to stored procedures or pass parameters to other functions like select, update etc. This is the default direction of a parameter. The OUT parameters are used to get values from stored procedures. The INOUT parameters are used to send values and retrieve values from stored procedures. 

### Parameter

The Parameter type represents a parameter for the SQL actions when a variable needs to be passed into the action.

## Package Contents