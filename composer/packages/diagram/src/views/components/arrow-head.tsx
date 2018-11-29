
import * as React from "react";

const ARROW_SIZE = 4;

export const ArrowHead: React.StatelessComponent<{
        x: number, y: number, direction: "left" | "right"
    }> = ({
        x, y, direction
    }) => {
        const p1 = { x, y};
        const p2 = { x: 0, y: 0};
        const p3 = { x: 0, y: 0};

        p2.x = (direction === "left") ? (x + 6) : (x - 6);
        p3.x = p2.x;
        p2.y = y - ARROW_SIZE;
        p3.y = y + ARROW_SIZE;

        return (
            <polyline className="arrow-head"
                points={`${p1.x},${p1.y} ${p2.x},${p2.y} ${p3.x},${p3.y} ${p1.x},${p1.y}`}
            />
        );
    };
