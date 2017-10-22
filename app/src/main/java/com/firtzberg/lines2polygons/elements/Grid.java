package com.firtzberg.lines2polygons.elements;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hrvoje on 04.10.17..
 * Set of lines on a 2D grid.
 */
public class Grid implements Parcelable {

    public static final Creator<Grid> CREATOR = new Creator<Grid>() {
        @Override
        public Grid createFromParcel(Parcel in) {
            try {
                return new Grid(in);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public Grid[] newArray(int size) {
            return new Grid[size];
        }
    };
    /**
     * Width of 2D grid.
     */
    public final float width;
    /**
     * Height of 2D grid.
     */
    public final float height;
    /**
     * Approved cuts in grid.
     */
    protected final List<Line> lines;

    /**
     * Creates a new grid to which cuts and folds can be added.
     *
     * @param width  Width of grid.
     * @param height Height of grid.
     */
    public Grid(float width, float height) {
        this.width = width;
        this.height = height;
        lines = new ArrayList<>();
    }

    /**
     * Constructs grid from parcel.
     *
     * @param in Parcel containing grid fields.
     */
    protected Grid(Parcel in) throws Exception {
        width = in.readFloat();
        height = in.readFloat();
        lines = in.createTypedArrayList(Line.CREATOR);
    }

    /**
     * Adds new line to grid.
     *
     * @param line Line to be added.
     */
    public void addLine(Line line) {
        lines.add(line);
    }

    /**
     * Gets current lines.
     *
     * @return Iterable over current lines.
     */
    public Iterable<Line> getLines() {
        return lines;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeFloat(width);
        parcel.writeFloat(height);
        parcel.writeTypedList(lines);
    }
}
