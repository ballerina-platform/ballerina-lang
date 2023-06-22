import ballerina/module1;

function demo() {
}

service / on new module1
# This returns list of services in all namespaces.
# + return - list of services in namespaces.
function getServicesListFromK8s() returns ServiceList|error {
    return {list: getServicesList(), pagination: {total: getServicesList().length()}};
}

function myFunc() { 

}