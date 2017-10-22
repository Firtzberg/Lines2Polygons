package com.firtzberg.lines2polygons;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.firtzberg.lines2polygons.drawing.GridView;
import com.firtzberg.lines2polygons.drawing.GridWithHistory;
import com.firtzberg.lines2polygons.elements.Grid;
import com.firtzberg.lines2polygons.elements.Line;
import com.firtzberg.lines2polygons.elements.Point;

public class LinesActivity extends Activity implements GridWithHistory.GridObserver {

    /**
     * Key with witch the grid with history is save during rotation.
     */
    static final String PARCEL_GRID_KEY = "grid";

    private GridView gridView;
    private View redoButton;
    private View undoButton;
    private View drawModeButton;
    private View eraseModeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lines);

        gridView = (GridView) findViewById(R.id.grid);
        redoButton = findViewById(R.id.redo);
        undoButton = findViewById(R.id.undo);
        drawModeButton = findViewById(R.id.draw);
        eraseModeButton = findViewById(R.id.erase);

        if (savedInstanceState == null) {
            GridWithHistory grid = new GridWithHistory(10, 10);
            grid.addLine(new Line(new Point(0, 3), new Point(7, 3)));
            grid.addLine(new Line(new Point(7, 3), new Point(7, 10)));
            grid.addLine(new Line(new Point(5, 0), new Point(5, 5)));
            grid.addLine(new Line(new Point(5, 5), new Point(10, 5)));
            gridView.setGrid(grid);
            grid.subscribe(this);
        }
    }

    /**
     * Finishes editing of the grid and sends it to the OpenGLActivity.
     *
     * @param view button which triggered the submit action.
     */
    public void submit(View view) {
        Intent intent = new Intent();
        intent.putExtra(OpenGLActivity.PARCELABLE_GRID_KEY, gridView.getGrid());
        intent.setClass(this, OpenGLActivity.class);
        startActivity(intent);
    }

    /**
     * Undoes an action if undo was pressed, redoes otherwise.
     *
     * @param view View witch triggered the action.
     */
    public void unredo(View view) {
        if (view.getId() == R.id.undo) {
            gridView.getGrid().Undo();
        } else {
            gridView.getGrid().Redo();
        }
    }

    /**
     * updates the grid view mode in accordance with pressed button.
     *
     * @param view The view witch triggered the action.
     */
    public void setMode(View view) {
        com.firtzberg.lines2polygons.drawing.GridView.Mode type;
        switch (view.getId()) {
            case R.id.draw:
                type = GridView.Mode.Draw;
                break;
            case R.id.erase:
                type = GridView.Mode.Erase;
                break;
            default:
                return;
        }
        gridView.setMode(type);
        onChange(null);
    }

    @Override
    public void onChange(Grid grid) {
        gridView.invalidate();
        undoButton.setEnabled(gridView.getGrid().canUndo());
        redoButton.setEnabled(gridView.getGrid().canRedo());
        drawModeButton.setEnabled(gridView.getMode() != GridView.Mode.Draw);
        eraseModeButton.setEnabled(gridView.getMode() != GridView.Mode.Erase);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        GridWithHistory grid = savedInstanceState.getParcelable(PARCEL_GRID_KEY);
        gridView.setGrid(grid);
        grid.subscribe(this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelable(PARCEL_GRID_KEY, gridView.getGrid());

    }
}
