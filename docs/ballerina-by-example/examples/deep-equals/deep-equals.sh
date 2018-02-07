$ ballerina run deep-equals.bal
# Deep equals for strings
helloString1 has the same value as helloString2
helloString1 is not equal to welcomeString2

# Deep equals for int
Integer values are equal
5 is not equal to 5

# Deep equals for arrays. Order of the values are considered.
floatValues1 has the values as floatValues2 and are in order.
Array has different values.

# Deep equals for structs. Order of the fields does not matter. The order of the values of an array field does matter.
This is nick even if fields are unordered.
Nick is not Jack.
Address of Nick is different.

# Deep equals for maps. The order of the attributes are always considered. Values of array attributes should be in order.
map1 and map2 has the same values and are in the right order.
The values of the map attributes are different.

# Deep equals for any type.
any1 and any2 are equal as they have the same values.
5 is not equal to 10.
5 is not a string.
5 is not an array.

# Deep Equals for JSON. The order of the members does not matter.
It is the same store.
Address of the store is different.
