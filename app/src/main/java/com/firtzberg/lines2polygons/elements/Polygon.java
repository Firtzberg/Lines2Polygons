package com.firtzberg.lines2polygons.elements;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hrvoje on 22.10.17..
 * Set of line sides.
 */
public class Polygon {

    /**
     * Lines enclosing polygon area.
     */
    protected final List<LineSide> sides;
    /**
     * True when polygon is closed.
     */
    private boolean complete = false;
    /**
     * Minimal set of points enclosing polygon area or null when polygon is incomplete.
     */
    private Point[] optimisedBorders = null;

    /**
     * Creates an incomplete polygon without any line sides.
     */
    public Polygon() {
        sides = new ArrayList<>();
        Log.d("Polygon construction", "New polygon");
    }

    /**
     * Adds a new side to the polygon.
     *
     * @param lineSide Line side to be occupied by the polygon.
     * @return True if line side is successfully appended to the previous line sides,
     * false if polygon is already complete or
     * start point of line side does not match end point of last line side.
     */
    public final boolean addSide(LineSide lineSide) {
        if (complete)
            return false;
        if (lineSide == null)
            return false;
        if (!sides.isEmpty() && !sides.get(sides.size() - 1).line.end.equals(lineSide.line.start))
            return false;
        lineSide.setAttachedPolygon(this);
        sides.add(lineSide);
        Log.d("Polygon construction", "Added side " + lineSide.line);
        complete = sides.get(0).line.start.equals(lineSide.line.end);
        if (complete) {
            Log.d("Polygon construction", "Completed");
            optimisedBorders = optimiseBorders();
        }
        return true;
    }

    /**
     * Gets start points of line sides skipping points which are between adjacent points.
     *
     * @return Array of start points of line sides skipping points which are between adjacent points.
     */
    private Point[] optimiseBorders() {
        ArrayList<Point> points = new ArrayList<>(sides.size());
        Line previousLine = sides.get(sides.size() - 1).line;
        Line currentLine;
        for (int sideIndex = 0; sideIndex < sides.size(); sideIndex++) {
            currentLine = sides.get(sideIndex).line;
            if (!previousLine.vector.sameOrientation(currentLine.vector))
                points.add(currentLine.start);
            else
                Log.d("Border optimisation", "Point " + currentLine.start + " between " + previousLine.start + " and " + currentLine.end + " skipped.");
            previousLine = currentLine;
        }

        Point[] result = new Point[points.size()];
        points.toArray(result);
        return result;
    }

    /**
     * Checks whether polygon is complete.
     *
     * @return True when sides create closed loop, false otherwise.
     */
    public final boolean isComplete() {
        return complete;
    }

    /**
     * Gets edges of the polygon.
     * <p>Do not edit the array.</p>
     *
     * @return Null if polygon is incomplete or edges of polygon.
     * @
     */
    public final Point[] getBorders() {
        return optimisedBorders;
    }

    /**
     * One side of a line. Allows polygons to easily get the polygon on the other side.
     * If this functionality is useless, feel free to use lines instead.
     */
    public static class LineSide {
        /**
         * Directed line.
         */
        public final Line line;
        /**
         * Other side which has the same line in the opposite direction.
         */
        public final LineSide otherSide;
        /**
         * Polygon to which this line side belongs.
         */
        private Polygon attachedPolygon;

        /**
         * Creates a new line side using the provided line and
         * creates the opposite line side by reverting the line.
         * both side are not attached to any polygon.
         *
         * @param line Line for which the two line sides will be created.
         */
        public LineSide(Line line) {
            this.line = line;
            otherSide = new LineSide(this);
        }

        /**
         * Creates the opposite line side.
         *
         * @param otherSide The line side for which the apposite side should be created.
         */
        private LineSide(LineSide otherSide) {
            this.line = new Line(otherSide.line.end, otherSide.line.start);
            this.otherSide = otherSide;
        }

        /**
         * Gets the polygon to which the line side belongs.
         *
         * @return The polygon to which this line side belongs or
         * null if no polygon is constructed using this line side.
         */
        public Polygon getAttachedPolygon() {
            return attachedPolygon;
        }

        /**
         * Sets the polygon to which the line side belongs.
         *
         * @param attachedPolygon The polygon to which this line side is successfully added.
         * @throws UnsupportedOperationException if already attached to a polygon.
         */
        public void setAttachedPolygon(Polygon attachedPolygon) {
            if (this.attachedPolygon != null)
                throw new UnsupportedOperationException();
            this.attachedPolygon = attachedPolygon;
        }
    }
}
