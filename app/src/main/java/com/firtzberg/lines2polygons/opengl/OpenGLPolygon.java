package com.firtzberg.lines2polygons.opengl;

import android.opengl.GLES20;

import com.firtzberg.lines2polygons.conversion.Triangulator;
import com.firtzberg.lines2polygons.elements.Point;
import com.firtzberg.lines2polygons.elements.Polygon;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

/**
 * Created by hrvoje on 21.10.17..
 * Container of all data needed to draw a polygon in OpenGL ES.
 */
public class OpenGLPolygon {
    /**
     * Number of bytes per float.
     */
    public static final int BYTES_PER_FLOAT = 4;
    /**
     * Number of floats per vertex coordinate.
     */
    public static final int FLOATS_PER_VERTEX = 3;
    /**
     * Number of floats per color.
     */
    public static final int FLOATS_PER_COLOR = 4;
    /**
     * Buffer for colors of triangle vertices.
     */
    final FloatBuffer trianglesColorBuffer;
    /**
     * Buffer for positions of triangle vertices.
     */
    final FloatBuffer trianglesPositionBuffer;
    /**
     * Buffer for positions of border points.
     */
    final FloatBuffer borderBuffer;
    /**
     * Number of border points.
     */
    final int borderVertexCount;
    /**
     * Number of vertices on all triangles.
     */
    final int triangleVertexCount;

    /**
     * Does all polygon specific preparations for drawing it using OpenGL ES except setting the color.
     *
     * @param polygon Polygon from which buffers will be prepared.
     */
    public OpenGLPolygon(Polygon polygon) {
        Point[] points = polygon.getBorders();
        float[] vertices = new float[points.length * 2];
        borderBuffer = ByteBuffer.allocateDirect(points.length * FLOATS_PER_VERTEX * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        int i = 0;
        //Log.d("Border", "start");
        for (Point point :
                points) {
            vertices[i++] = point.x;
            borderBuffer.put(point.x);
            vertices[i++] = -point.y;
            borderBuffer.put(-point.y);
            borderBuffer.put(0);
            //Log.d("Border", "x: "+point.x+", y: "+ -point.y);
        }
        //Log.d("Border", "end");
        borderVertexCount = points.length;
        Triangulator triangulator = new Triangulator();
        List<Short> vertexIndices = triangulator.computeTriangles(vertices);

        trianglesPositionBuffer = ByteBuffer.allocateDirect(vertexIndices.size() * FLOATS_PER_VERTEX * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        trianglesColorBuffer = ByteBuffer.allocateDirect(vertexIndices.size() * FLOATS_PER_COLOR * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        //Log.d("Triangle", "start");
        for (Short index :
                vertexIndices) {
            trianglesPositionBuffer.put(vertices[2 * index]);
            trianglesPositionBuffer.put(vertices[2 * index + 1]);
            trianglesPositionBuffer.put(0);
            //Log.d("Triangle", "x: " + points[index].x + ", y: " + -points[index].y);
        }
        //Log.d("Triangle", "end");
        triangleVertexCount = vertexIndices.size();

        trianglesPositionBuffer.position(0);
        borderBuffer.position(0);
    }

    /**
     * Sets the color of the whole polygon.
     *
     * @param r Red from 0 to 1.
     * @param g Green from 0 to 1.
     * @param b Blue from 0 to 1.
     */
    public void setColor(float r, float g, float b) {
        trianglesColorBuffer.position(0);
        for (int i = 0; i < triangleVertexCount; i++) {
            trianglesColorBuffer.put(r);
            trianglesColorBuffer.put(g);
            trianglesColorBuffer.put(b);
            trianglesColorBuffer.put(1.0f);
        }
    }

    /**
     * Gets the number of triangle vertices.
     *
     * @return Number of triangle vertices after triangulation.
     */
    public int getTriangleVertexCount() {
        return triangleVertexCount;
    }

    /**
     * Gets the number of points creating the border.
     *
     * @return Number of border points.
     */
    public int getBorderVertexCount() {
        return borderVertexCount;
    }

    /**
     * Draws the border after color and other thing were configured.
     *
     * @param positionHandle Handle to position attribute.
     */
    public void border(final int positionHandle) {
        // let border color be set from renderer where a shared a shared buffer is used
        borderBuffer.position(0);
        GLES20.glVertexAttribPointer(positionHandle, FLOATS_PER_VERTEX, GLES20.GL_FLOAT, false,
                0, borderBuffer);

        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, borderVertexCount);
    }

    /**
     * Draws the triangles composing the polygon after other things were configured.
     *
     * @param positionHandle Handle to position attribute.
     * @param colorHandle    Handle to color attribute.
     */
    public void triangles(final int positionHandle, final int colorHandle) {

        trianglesColorBuffer.position(0);
        GLES20.glVertexAttribPointer(colorHandle, FLOATS_PER_COLOR, GLES20.GL_FLOAT, false,
                0, trianglesColorBuffer);

        GLES20.glEnableVertexAttribArray(colorHandle);

        trianglesPositionBuffer.position(0);
        GLES20.glVertexAttribPointer(positionHandle, FLOATS_PER_VERTEX, GLES20.GL_FLOAT, false,
                0, trianglesPositionBuffer);

        GLES20.glEnableVertexAttribArray(positionHandle);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, triangleVertexCount);
    }
}
