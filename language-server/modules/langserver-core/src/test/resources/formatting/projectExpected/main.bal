function getDateFormatIncludingDayName(string year, int month, int day, 
                                      int dayNumber) returns string {
  string? text = ();
  match dayNumber {
    1 => { text = "Sunday"; }
    2 => { text = "Monday"; }
    3 => { text = "Tuesday"; }
    4 => { text = "Wednesday"; }
    5 => { text = "Thursday"; }
    6 => { text = "Friday"; }
    7 => { text = "Saturday"; }
  }
  if text !is () {
    return string `${year} ${month} ${text} ${day}`;
  }
  else { return string `${year} ${month} ${day}`; }
}
