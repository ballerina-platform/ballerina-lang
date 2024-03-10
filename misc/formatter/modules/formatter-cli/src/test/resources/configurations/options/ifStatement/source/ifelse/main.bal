function getGrades(int score) returns string  {
    int i = 0;
    if 0 <  score && score < 55 {
        return "F";
    } else   if 55 <= score  && score < 65  {
        return "C"  ;
    } else  {
        return "Invalid grade";
    }
}
