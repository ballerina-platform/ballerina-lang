import ballerina/accuweather;
public function main() {
accuweather:Client accuweatherClient = new ("8dbh68Zg2J6WxAK37Cy2jVJTSMdnyAmV");
accuweather:WeatherResponse accuweatherResult = check accuweatherClient->getDailyWeather("80000");
}
