import classNames from "classnames";
import * as React from "react";

const ARROW_SIZE = 6;

export const ArrowHead: React.StatelessComponent<{
        x: number, y: number, direction: "left" | "right", className?: string
    }> = ({
        x, y, direction, className
    }) => {
        const p1 = { x, y};
        const p2 = { x: 0, y: 0, rx: 12, ry: 12};
        const p3 = { x: 0, y: 0, rx: 12, ry: 12};

        p2.x = (direction === "left") ? (x + 6) : (x - 6);
        p3.x = p2.x;
        p2.y = y - ARROW_SIZE;
        p3.y = y + ARROW_SIZE;

        return (
            <polyline className={classNames("arrow-head", className)}
                points={`${p1.x},${p1.y} ${p2.x},${p2.y} ${p3.x},${p3.y} ${p1.x},${p1.y}`}
            />
        );
    };
