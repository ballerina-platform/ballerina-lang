$ curl -v http://localhost:9090/hbr/route -H "type:location"
{
    "name": "Colombo,Sri Lanka",
    "longitude": -556.49,
    "latitude": 257.76,
    "altitude": 230,
}

$ curl -v http://localhost:9090/hbr/route -H "type:weather"
{"coord":{"lon":139.01,"lat":35.02},"weather":[{"id":800,
"main":"Clear","description":"clear sky","icon":"01n"}],
"base":"station","main":{"temp":25.51,"clouds":{"all":0},
"wind":{"speed":5.52,"deg":311},"dt":148579296788555885,
"id":1907296,"name":"Tawarano","cod":200}}