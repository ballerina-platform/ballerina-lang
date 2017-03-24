import normal from "./normal";

export default function() {
  var randomNormal = normal.apply(this, arguments);
  return function() {
    return Math.exp(randomNormal());
  };
}
