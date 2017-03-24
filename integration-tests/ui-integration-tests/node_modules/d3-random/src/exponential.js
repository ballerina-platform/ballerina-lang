export default function(lambda) {
  return function() {
    return -Math.log(1 - Math.random()) / lambda;
  };
}
