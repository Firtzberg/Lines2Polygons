package com.firtzberg.lines2polygons.drawing;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.firtzberg.lines2polygons.R;
import com.firtzberg.lines2polygons.elements.Line;
import com.firtzberg.lines2polygons.elements.Point;

/**
 * Custom view showing grid and handling line drawing and erasing.
 */
public class GridView extends View {

    /**
     * Color of paper when not set in attributes.
     */
    private static final int DEFAULT_PAPER_COLOR = Color.WHITE;
    /**
     * Color of grid lines when not set in attributes.
     */
    private static final int DEFAULT_LINE_COLOR = Color.BLACK;
    /**
     * Color of candidate line when no set in attributes.
     */
    private static final int DEFAULT_CANDIDATE_COLOR = Color.BLUE;
    /**
     * Displayed grid.
     */
    protected GridWithHistory grid;
    Line candidate;
    /**
     * Current editing mode.
     */
    private Mode mode = Mode.Draw;
    private float pixelsPerUnit;
    private float paddingLeft;
    private float paddingTop;

    /**
     * Paint for paper.
     */
    private Paint paperPaint;
    /**
     * Paint for lines added to grid and grid frame.
     */
    private Paint linePaint;
    /**
     * Paint for line candidate.
     */
    private Paint candidatePaint;
    /**
     * Gesture detector.
     */
    private GestureDetector detector;

    public GridView(Context context) {
        super(context);
        init(null, 0);
    }

    public GridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public GridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Set up a paints
        paperPaint = new Paint();
        paperPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paperPaint.setStyle(Paint.Style.FILL);

        linePaint = new Paint();
        linePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);

        candidatePaint = new Paint();
        candidatePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        candidatePaint.setStyle(Paint.Style.STROKE);

        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.GridView, defStyle, 0);

        paperPaint.setColor(a.getColor(
                R.styleable.GridView_paperColor,
                DEFAULT_PAPER_COLOR));
        linePaint.setColor(a.getColor(
                R.styleable.GridView_lineColor,
                DEFAULT_LINE_COLOR));
        candidatePaint.setColor(a.getColor(
                R.styleable.GridView_candidateColor,
                DEFAULT_CANDIDATE_COLOR));

        detector = new ExtendedGestureDetector(getContext(), new LineGestureListener());

        a.recycle();
    }

    /**
     * Gets current editing mode.
     *
     * @return Current editing mode.
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Sets editing mode.
     *
     * @param mode Editing mode to be set.
     */
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    /**
     * Gets displayed grid.
     *
     * @return Displayed grid.
     */
    public GridWithHistory getGrid() {
        return grid;
    }

    /**
     * Sets the displayed grid.
     *
     * @param grid Grid to be shown.
     */
    public void setGrid(GridWithHistory grid) {
        this.grid = grid;
        invalidate();
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (super.onTouchEvent(event)) return true;
        return detector.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        if (grid != null) {
            float pixelsPerHeightUnit = contentHeight / grid.height;
            float pixelsPerWidthUnit = contentWidth / grid.width;
            float overflow;
            if (pixelsPerHeightUnit < pixelsPerWidthUnit) {
                overflow = (pixelsPerWidthUnit - pixelsPerHeightUnit) * grid.width;
                contentWidth -= overflow;
                overflow /= 2;
                paddingLeft += overflow;

                pixelsPerUnit = pixelsPerHeightUnit;
                this.paddingLeft = paddingLeft;
                this.paddingTop = paddingTop;
            } else {
                overflow = (pixelsPerHeightUnit - pixelsPerWidthUnit) * grid.width;
                contentHeight -= overflow;
                overflow /= 2;
                paddingTop += overflow;

                pixelsPerUnit = pixelsPerWidthUnit;
                this.paddingLeft = paddingLeft;
                this.paddingTop = paddingTop;
            }
            paddingRight = paddingLeft + contentWidth;
            paddingBottom = paddingTop + contentHeight;

            // draw paper and frame
            canvas.drawRect(paddingLeft, paddingTop, paddingRight, paddingBottom, paperPaint);
            canvas.drawRect(paddingLeft, paddingTop, paddingRight, paddingBottom, linePaint);

            // draw lines
            for (Line line : grid.getLines()) {
                drawLine(canvas, line, linePaint);
            }
            // draw candidate
            if (candidate != null) {
                drawLine(canvas, candidate, candidatePaint);
            }
        }
    }

    /**
     * Draws a line on the canvas.
     *
     * @param canvas Canvas on which the line should be drawn.
     * @param line   Line to be drawn.
     * @param paint  Pain with which line is drawn.
     */
    protected void drawLine(Canvas canvas, Line line, Paint paint) {
        canvas.drawLine(getX(line.start), getY(line.start), getX(line.end), getY(line.end), paint);
    }

    /**
     * Calculates the x position in pixels of the given point.
     *
     * @param point Point for which the x position in pixels is calculated.
     * @return The x position in pixels.
     */
    protected float getX(Point point) {
        return paddingLeft + pixelsPerUnit * point.x;
    }

    /**
     * Calculates the y position in pixels of the given point.
     *
     * @param point Point for which the y position in pixels is calculated.
     * @return The y position in pixels.
     */
    protected float getY(Point point) {
        return paddingTop + pixelsPerUnit * point.y;
    }

    /**
     * Checks whether the pixel coordinates round up to the provided point.
     *
     * @param x     Current x coordinate in pixels.
     * @param y     Current y coordinate in pixels.
     * @param point Point against which the pixel positions are checked.
     * @return True if point is the closes point with integer values to the pixel coordinates.
     */
    protected boolean isSame(float x, float y, Point point) {
        return point.x == Math.round((x - paddingLeft) / pixelsPerUnit) && point.y == Math.round((y - paddingTop) / pixelsPerUnit);
    }

    /**
     * Gets the closes point with integer values to the position of the motion event.
     *
     * @param e Motion event.
     * @return Closest point with integer values.
     */
    protected Point getPoint(MotionEvent e) {
        return new Point(Math.round((e.getX() - paddingLeft) / pixelsPerUnit), Math.round((e.getY() - paddingTop) / pixelsPerUnit));
    }

    /**
     * Updates candidate line.
     *
     * @param line New candidate line.
     */
    public void onLineChanged(Line line) {
        candidate = line;
        invalidate();
    }

    /**
     * Applies line candidate to grid in current edit mode.
     */
    public void submitLine() {
        Line tmp = candidate;
        candidate = null;
        if (tmp != null && !tmp.start.equals(tmp.end)) {
            switch (mode) {

                case Draw:
                    grid.addLine(tmp);
                    break;
                case Erase:
                    grid.Erase(tmp);
                    // invalidate view to make sure candidate line is removed.
                    this.invalidate();
                    break;
            }
        }
    }

    /**
     * Editing modes
     */
    public enum Mode {
        Draw, Erase
    }

    /**
     * Gesture detector tracking gestures drawing lines.
     */
    private class LineGestureListener extends ExtendedGestureDetector.SimpleOnGestureListener {
        /**
         * Closest point with integer values to coordinates where scroll started.
         */
        Point startPoint;
        /**
         * Closest point with integer values to coordinates where scroll ends.
         */
        Point endPoint;

        @Override
        public boolean onDown(MotionEvent event) {
            // Start scrolling anywhere
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // initialise start point if not set.
            if (startPoint == null)
                startPoint = GridView.this.getPoint(e1);
            // Check if end point matches current coordinates.
            if (endPoint == null || !GridView.this.isSame(e2.getX(), e2.getY(), endPoint)) {
                // Update end point and notify view
                endPoint = GridView.this.getPoint(e2);
                GridView.this.onLineChanged(new Line(startPoint, endPoint));
            }
            return true;
        }

        @Override
        public boolean onFingerUp(MotionEvent e) {
            GridView.this.submitLine();
            endPoint = null;
            startPoint = null;
            return true;
        }
    }
}
