package com.firtzberg.lines2polygons.elements;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hrvoje on 07.10.17..
 * Immutable point in 2D float space.
 */
public class Point implements Parcelable {
    public static final Creator<Point> CREATOR = new Creator<Point>() {
        @Override
        public Point createFromParcel(Parcel in) {
            return new Point(in);
        }

        @Override
        public Point[] newArray(int size) {
            return new Point[size];
        }
    };
    /**
     * Maximal distance in a dimension between points considered equal.
     */
    public static final float GRANULARITY = 0.01f;
    /**
     * X coordinate.
     */
    public final float x;
    /**
     * Y coordinate.
     */
    public final float y;

    /**
     * Constructs point from coordinate values.
     *
     * @param x X coordinate.
     * @param y Y coordinate.
     */
    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Constructs point from parcel.
     *
     * @param in Parcel containing point fields.
     */
    protected Point(Parcel in) {
        x = in.readFloat();
        y = in.readFloat();
    }

    @Override
    public boolean equals(Object o) {
        // self check
        if (this == o)
            return true;
        // null check
        if (o == null)
            return false;
        // type check and cast
        if (getClass() != o.getClass())
            return false;
        Point point = (Point) o;
        return this.x > point.x - GRANULARITY && this.x < point.x + GRANULARITY &&
                this.y > point.y - GRANULARITY && this.y < point.y + GRANULARITY;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + Float.floatToIntBits(x);
        result = 31 * result + Float.floatToIntBits(y);
        return result;
    }

    @Override
    public String toString() {
        return "(" + String.valueOf(x) + ", " + String.valueOf(y) + ")";
    }

    /**
     * Creates a new Point, which is offset from this point by the given vecor.
     *
     * @param vector Vector by which the point will be offset.
     * @return Top point of vector when its origin is positioned at this point.
     */
    public Point move(Vector vector) {
        return new Point(this.x + vector.x, this.y + vector.y);
    }

    /**
     * Checks whether the point is within the given area.
     *
     * @param left   Minimal allowed value for X coordinate.
     * @param top    Minimal allowed value for Y coordinate.
     * @param right  Maximal allowed value for X coordinate.
     * @param bottom Maximal allowed value for Y coordinate.
     * @return True if point is within or at edge of the given area.
     */
    public boolean isInArea(int left, int top, int right, int bottom) {
        return x >= left && y >= top && x <= right && y <= bottom;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeFloat(x);
        parcel.writeFloat(y);
    }
}
