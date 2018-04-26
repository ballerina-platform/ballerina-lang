## Package overview

This package provides the common record types and constants required for other data management packages such as `jdbc`, `mysql`, and `h2`. 

### PoolOptions 

The PoolOptions type the properties that are used to configure DB connection pool. This is used with `jdbc`, `mysql`, and `h2` endpoints to configure the connection pool associated with the endpoint.

### SQLType

The SQLType represents the SQL data type of a given parameter. When using a parameter, use the same SQL type as the actual database table column type. Otherwise data loss can occur.

### Direction

The Direction type represents the direction of the parameter. IN parameters are used to send values to stored procedures or pass parameters to other functions like select, update, etc. This is the default direction of a parameter. The OUT parameters are used to get values from stored procedures. The INOUT parameters are used to send values and retrieve values from stored procedures. 

### Parameter

The Parameter type represents a parameter for the SQL actions when a variable needs to be passed into the action.
