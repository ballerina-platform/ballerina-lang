class Circle {
    float radius;
    function init(float radius) {
        self.radius = radius;
    }

    function getCircumference() returns float {
        return 2 * 'float:PI * self.radius;
    }

    function getArea() returns float {
        return float:PI * 'float:pow(self.radius, 2);
    }
}

public function test() {
    float circum = new Circle(10.5).getCircumference();

    float area = new Circle(12).
}
