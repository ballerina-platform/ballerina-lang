map<string[]> riderRequestedLocationMap = {};

service class RiderService {

    remote function onMessage(string id) returns error? {
        riderRequestedLocationMap.get(id).
    }
}
