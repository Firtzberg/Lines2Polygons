package com.firtzberg.lines2polygons.elements;

/**
 * Created by hrvoje on 07.10.17..
 * Immutable vector in 2D float space.
 */
public class Vector extends Point {

    /**
     * Constructs a new vector.
     *
     * @param x X coordinate.
     * @param y Y coordinate.
     */
    public Vector(float x, float y) {
        super(x, y);
    }

    /**
     * Constructs a new vector.
     *
     * @param origin Origin of vector.
     * @param top    Top of vector.
     */
    public Vector(Point origin, Point top) {
        super(top.x - origin.x, top.y - origin.y);
    }

    /**
     * Checks whether vectors are parallel.
     *
     * @param vector The other vector against which the parallel check should be done.
     * @return True when this and vector are parallel, false otherwise.
     */
    public boolean isParallel(Vector vector) {
        float span = this.x * vector.y - vector.x * this.y;
        return -GRANULARITY * GRANULARITY < span && span < GRANULARITY * GRANULARITY;
    }

    /**
     * Check whether vectors are parallel and face the same orientation.
     *
     * @param vector The other vector against which the orientation check should be done.
     * @return rue when this and vector are parallel and oriented in the same direction.
     */
    public boolean sameOrientation(Vector vector) {
        if (!isParallel(vector))
            return false;
        if (x < -GRANULARITY) {
            return vector.x <= GRANULARITY;
        } else if (x > GRANULARITY) {
            return vector.x >= -GRANULARITY;
        } else {
            if (y < -GRANULARITY) {
                return vector.y <= GRANULARITY;
            } else if (y > GRANULARITY) {
                return vector.y >= -GRANULARITY;
            } else {
                return true;
            }
        }
    }

    /**
     * Creates a new vector with same orientation factor times longer.
     *
     * @param factor The factor by which the resulting vector is longer.
     * @return New vector with same orientation factor times longer.
     */
    public Vector scale(float factor) {
        return new Vector(x * factor, y * factor);
    }

    /**
     * Calculates the manhattan distance of vector.
     *
     * @return Manhattan distance of vector.
     */
    public int manhattanDistance() {
        int distance = 0;
        if (x > 0)
            distance += x;
        else
            distance -= x;
        if (y > 0)
            distance += y;
        else
            distance -= y;
        return distance;
    }

    /**
     * Gets the angle between this vector direction and vector (1,0).
     *
     * @return Angle in radians from -pi to pi.
     */
    public double getAngle() {
        return Math.atan2(y, x);
    }
}
