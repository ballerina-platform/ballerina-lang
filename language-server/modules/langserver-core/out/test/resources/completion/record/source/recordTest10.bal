type Address record {|
    string city = "";
    string country = "";
    record {
        int countryCode = 0;
        int phoneNo = 0;
    } phone = {
        
    };
|};