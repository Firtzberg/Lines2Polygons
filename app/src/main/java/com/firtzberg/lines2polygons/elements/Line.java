package com.firtzberg.lines2polygons.elements;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hrvoje on 04.10.17..
 * Immutable straight line fragment with start and end point in 2D space.
 */
public class Line implements Parcelable {
    public static final Creator<Line> CREATOR = new Creator<Line>() {
        @Override
        public Line createFromParcel(Parcel in) {
            return new Line(in);
        }

        @Override
        public Line[] newArray(int size) {
            return new Line[size];
        }
    };
    /**
     * Start point.
     */
    public final Point start;
    /**
     * End point.
     */
    public final Point end;
    /**
     * Vector from start to end.
     */
    public final Vector vector;
    /**
     * Last calculated value of hash code. 0 if not calculated earlier.
     */
    private int hashCode = 0;

    /**
     * Constructs new line.
     *
     * @param startPoint Start point of line.
     * @param endPoint   End point of line.
     */
    public Line(Point startPoint, Point endPoint) {
        start = startPoint;
        end = endPoint;
        vector = new Vector(start, end);
    }

    /**
     * Constructs line from parcel.
     *
     * @param in Parcel containing line fields.
     */
    protected Line(Parcel in) {
        start = in.readParcelable(Point.class.getClassLoader());
        end = in.readParcelable(Point.class.getClassLoader());
        vector = new Vector(start, end);
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
        Line line = (Line) o;
        return this.start.equals(line.start) && this.end.equals(line.end);
    }

    @Override
    public int hashCode() {
        if (hashCode != 0)
            return hashCode;
        int result = 17;
        result = 31 * result + start.hashCode();
        result = 31 * result + end.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return start.toString() + "->" + end.toString();
    }

    /**
     * Checks whether the line contains the given point.
     *
     * @param point          The point which might be a part of the line.
     * @param includingEdges True when start and end point are considered contained, false otherwise.
     * @return True when line contains point point or point is same as start or end point, false otherwise.
     */
    public boolean contains(Point point, boolean includingEdges) {
        Vector offset = new Vector(start, point);
        if (!vector.sameOrientation(offset))
            return false;
        int lineLength = vector.manhattanDistance();
        int offsetLength = offset.manhattanDistance();
        if (includingEdges)
            return offsetLength <= lineLength;
        else return offsetLength > 0 && offsetLength < lineLength;
    }

    /**
     * Checks whether lines have more than one common point, i.e. partially overlap.
     *
     * @param line Other line possibly overlapping this line.
     * @return True when lines have more than one shared point, false otherwise.
     */
    public boolean overlap(Line line) {
        int sharedPoints = 0;
        if (this.contains(line.start, true))
            sharedPoints++;
        if (this.contains(line.end, true))
            sharedPoints++;
        if (sharedPoints == 2)
            return true;
        if (line.contains(this.start, false))
            sharedPoints++;
        if (sharedPoints == 2)
            return true;
        if (line.contains(this.end, false))
            sharedPoints++;
        return sharedPoints > 1;
    }

    /**
     * Return intersection point of this and the other line.
     *
     * @param line           Other line intersecting this line.
     * @param includingEdges True when start and end point are candidates for intersection points, false otherwise.
     * @return Intersection point of this and the other line or null when lines are parallel or do not intersect.
     */
    public Point intersection(Line line, boolean includingEdges) {
        float div = line.vector.y * vector.x - line.vector.x * vector.y;
        if (div == 0)
            return null;

        // calculate differences
        Vector offset = new Vector(start, line.start);

        float ua = (line.vector.y * offset.x - line.vector.x * offset.y) / div;
        Point pt = start.move(vector.scale(ua));

        if (contains(pt, includingEdges) && line.contains(pt, includingEdges))
            return pt;
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(start, i);
        parcel.writeParcelable(end, i);
    }
}
