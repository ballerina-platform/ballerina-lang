import irwinHall from "./irwinHall";

export default function(n) {
  var randomIrwinHall = irwinHall(n);
  return function() {
    return randomIrwinHall() / n;
  };
}
