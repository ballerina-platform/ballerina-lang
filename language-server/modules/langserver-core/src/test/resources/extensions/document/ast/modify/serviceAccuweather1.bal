import ballerina/http;
import ballerina/accuweather;
service hello on new http:Listener(9090) {
    resource function sayHello(http:Caller caller, http:Request req) {
accuweather:Client accuweatherClient = new ("8dbh68Zg2J6WxAK37Cy2jVJTSMdnyAmV");
accuweather:WeatherResponse accuweatherResult = check accuweatherClient->getDailyWeather("80000");
    }
}
