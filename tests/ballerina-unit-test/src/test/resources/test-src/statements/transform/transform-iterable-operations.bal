struct Person {
    string name;
    Vehicle [] vehicles;
}

struct Vehicle {
    string vid; string vehicleType;
}

struct VehicleInfo {
    int numberOfCars;
    int numberOfBikes;
    string [] ids;
}

transformer<Person source,VehicleInfo target> foo() {
    target.numberOfCars = source.vehicles.filter(function(Vehicle vehicle) returns boolean {
                                                     return vehicle.vehicleType == "car";
                                                 }).count();

    target.numberOfBikes = source.vehicles.filter(function(Vehicle vehicle) returns boolean {
                                                      return vehicle.vehicleType == "bike";
                                                  }).count();

    target.ids = source.vehicles.map(function(Vehicle vehicle) returns string {
                                         return vehicle.vid;
                                     });
}

function testTransformerIterableOperations() returns (int, int, int) {
    Person person1 = {  name:"john",
                         vehicles:[{vid: "01", vehicleType:"car"},
                                   {vid: "02", vehicleType:"bike"},
                                   {vid: "03", vehicleType:"car"}
                                  ]};
    VehicleInfo vInfo = <VehicleInfo, foo()> person1;
    return (vInfo.numberOfCars, vInfo.numberOfBikes, lengthof(vInfo.ids));
}

