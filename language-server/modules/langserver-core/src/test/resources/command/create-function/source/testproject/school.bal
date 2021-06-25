type Grade record {
    string name;
}
    
School school = getSchool();

function myFunc() {
    School school2 = let Grade grade = {name:"Grade 1"} in createSchool([grade]);
    School school3 = let string str = getStr() in "Name:".concat(" ", str);
    let int id = 1 in getSchool(id);
}
