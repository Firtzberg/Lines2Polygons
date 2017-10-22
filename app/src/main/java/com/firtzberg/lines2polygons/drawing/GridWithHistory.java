package com.firtzberg.lines2polygons.drawing;

import com.firtzberg.lines2polygons.elements.Grid;
import com.firtzberg.lines2polygons.elements.Line;

import java.util.Collections;
import java.util.Stack;

/**
 * Created by hrvoje on 15.10.17..
 * Grid with lines keeping track of history states and notifying observers on change.
 */
public class GridWithHistory extends Grid {

    /**
     * Stack of previous sets of lines.
     */
    private final Stack<Line[]> previousStates;
    /**
     * Stack of undone sets of lines.
     */
    private final Stack<Line[]> undoneStates;
    /**
     * Observer to be notified when set of lines changes.
     */
    private GridObserver observer;

    /**
     * Creates new grid with history states with specified dimensions.
     *
     * @param width  Grid width.
     * @param height Grid height.
     */
    public GridWithHistory(float width, float height) {
        super(width, height);
        previousStates = new Stack<>();
        undoneStates = new Stack<>();
    }

    /**
     * Sets the observer which will be notified when the lines of this grid change.
     *
     * @param observer Observer to be notified.
     */
    public void subscribe(GridObserver observer) {
        this.observer = observer;
        if (observer != null) observer.onChange(this);
    }

    /**
     * Checks whether an action can be undone.
     *
     * @return True when action can be undone, false otherwise.
     */
    public boolean canUndo() {
        return !previousStates.isEmpty();
    }

    /**
     * Checks whether an action can be redone.
     *
     * @return True when action can be redone, false otherwise.
     */
    public boolean canRedo() {
        return !undoneStates.isEmpty();
    }

    /**
     * Undoes an action.
     *
     * @return True if action was undone, false otherwise.
     */
    public boolean Undo() {
        if (previousStates.isEmpty())
            return false;

        // save current state
        Line[] state = new Line[lines.size()];
        lines.toArray(state);
        undoneStates.push(state);

        // set previous state
        lines.clear();
        Collections.addAll(lines, previousStates.pop());

        // notify observer
        if (observer != null) observer.onChange(this);
        return true;
    }

    /**
     * Redoes an action.
     *
     * @return True if action was redone, false otherwise.
     */
    public boolean Redo() {
        if (undoneStates.isEmpty())
            return false;
        // save current state
        Line[] state = new Line[lines.size()];
        lines.toArray(state);
        previousStates.push(state);

        // set state
        lines.clear();
        Collections.addAll(lines, undoneStates.pop());

        // notify observer
        if (observer != null) observer.onChange(this);
        return true;
    }

    @Override
    public void addLine(Line line) {
        // save current state
        Line[] state = new Line[lines.size()];
        lines.toArray(state);
        previousStates.push(state);

        super.addLine(line);

        // clear undone moves
        undoneStates.clear();

        // notify observer
        if (observer != null) observer.onChange(this);
    }

    public void Erase(Line rubber) {
        // Preserve initial state to store it to the previous in case of change.
        Line[] state = new Line[lines.size()];
        lines.toArray(state);
        boolean changed = false;

        // Remove overlapped lines.
        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            if (line.overlap(rubber)) {
                lines.remove(i--);
                changed = true;
                Line toStart;
                Line toEnd;
                // Preserve line fragment around start point of incompletely erased line
                if (!rubber.contains(line.start, true)) {
                    toStart = new Line(line.start, rubber.start);
                    toEnd = new Line(line.start, rubber.end);
                    if (toStart.vector.manhattanDistance() < toEnd.vector.manhattanDistance()) {
                        lines.add(toStart);
                    } else {
                        lines.add(toEnd);
                    }
                }
                // Preserve line fragment around end point of incompletely erased line
                if (!rubber.contains(line.end, true)) {
                    toStart = new Line(line.end, rubber.start);
                    toEnd = new Line(line.end, rubber.end);

                    if (toStart.vector.manhattanDistance() < toEnd.vector.manhattanDistance()) {
                        lines.add(toStart);
                    } else {
                        lines.add(toEnd);
                    }
                }
            }
        }
        // if change occurred save initial state to previous states
        if (changed) {
            previousStates.push(state);
            undoneStates.clear();
            if (observer != null) observer.onChange(this);
        }
    }

    /**
     * Observes changes in a grid.
     */
    public interface GridObserver {
        /**
         * Callback triggered when line set in grid changes.
         *
         * @param grid Grid whose lines changed.
         */
        void onChange(Grid grid);

    }
}
