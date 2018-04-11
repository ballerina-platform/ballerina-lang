$ ballerina run foreach.bal
Iterating over a string array
#Only one value was defined, therefore, only the values are printed.
fruit: apple
fruit: banana
fruit: cherry

Iterating over a map.
#Two values were defined, therefore, both the key and the value in the map are printed.
letter: a, word: apple
letter: b, word: banana
letter: c, word: cherry

Iterating over a json object
#Returns the values in the JSON object.
value: apple
value: ["red","green"]
value: 5

Iterating over a json array
color 0 : red
color 1 : green

Iterating over a xml
#Two variables were defined, therefore, the Index and the value are printed.
xml at 0 : <name>Sherlock Holmes</name>
xml at 1 : <author>Sir Arthur Conan Doyle</author>

Iterating over an integer range
#Prints the sum of the integers for the defined range.
summation from 1 to 10 is :55
