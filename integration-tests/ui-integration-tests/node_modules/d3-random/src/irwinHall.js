export default function(n) {
  return function() {
    for (var sum = 0, i = 0; i < n; ++i) sum += Math.random();
    return sum;
  };
}
