$ curl -v http://localhost:9090/cbr/route -d '{"name" : "sanFrancisco"}' -H "Content-Type:application/json"
{
    "name": "San Francisco Test Station,USA",
    "longitude": -122.43,
    "latitude": 37.76,
    "altitude": 150,
    "rank": 1
}

$ curl -v http://localhost:9090/cbr/route -d '{"name" : "london"}' -H "Content-Type:application/json"
{
    "name": "London Test Station,England",
    "longitude": -156.49,
    "latitude": 57.76,
    "altitude": 430,
    "rank": 5
}