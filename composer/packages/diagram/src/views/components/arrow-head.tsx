import * as React from "react";
import classNames from "classnames";

const ARROW_SIZE = 3;

export const ArrowHead: React.StatelessComponent<{
        x: number, y: number, direction: "left" | "right", condition?: boolean
    }> = ({
        x, y, direction, condition
    }) => {
        const p1 = { x, y};
        const p2 = { x: 0, y: 0};
        const p3 = { x: 0, y: 0};

        p2.x = (direction === "left") ? (x + 6) : (x - 6);
        p3.x = p2.x;
        p2.y = y - ARROW_SIZE;
        p3.y = y + ARROW_SIZE;

        const arrowClass = (condition) ? "condition-arrow-head" : "";

        return (
            <polyline className={classNames("arrow-head", arrowClass)}
                points={`${p1.x},${p1.y} ${p2.x},${p2.y} ${p3.x},${p3.y} ${p1.x},${p1.y}`}
            />
        );
    };
