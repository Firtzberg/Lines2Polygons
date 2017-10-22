package com.firtzberg.lines2polygons.conversion;

import android.util.Log;

import com.firtzberg.lines2polygons.elements.Grid;
import com.firtzberg.lines2polygons.elements.Line;
import com.firtzberg.lines2polygons.elements.Point;
import com.firtzberg.lines2polygons.elements.Polygon;
import com.firtzberg.lines2polygons.elements.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hrvoje on 15.10.17..
 * Provides functionality for decomposing a grid into polygons.
 */
public class Polygonisation {

    /**
     * Converts a grid into a set of polygons.
     *
     * @param grid The grid to be decomposed.
     * @return Areas enclosed by the lines and frame of the grid including a polygon around the grid.
     */
    public static List<Polygon> gridToPolygons(Grid grid) {
        return nodesToPolygons(linesToNodes(decomposeGrid(grid)));
    }

    /**
     * Converts a grid into a set of non intersecting lines by splitting all lines at intersection points.
     *
     * @param grid Frame and set f ines to be decomposed.
     * @return Set of non intersecting lines.
     */
    private static Iterable<Line> decomposeGrid(Grid grid) {
        // Set of non intersecting lines so far extracted from the grid
        final List<Line> fragments = new ArrayList<>();
        // Frame edges
        Point tl = new Point(0, 0);
        Point tr = new Point(grid.width, 0);
        Point bl = new Point(0, grid.height);
        Point br = new Point(grid.width, grid.height);
        // Frame lines
        fragments.add(new Line(tl, tr));
        fragments.add(new Line(tl, bl));
        fragments.add(new Line(br, bl));
        fragments.add(new Line(br, tr));
        // Set of line fragments created during the process of adding a line to fragments.
        final List<Line> fragmentCandidates = new ArrayList<>();
        int existingFragmentsCount;
        int candidateCount;
        Line existingFragment;
        Line fragmentCandidate;
        Point intersection;
        for (Line line : grid.getLines()) {
            // Start with single fragment candidate.
            fragmentCandidates.clear();
            fragmentCandidates.add(line);

            // Since lines cannot intersect at multiple places
            // do not check for additional intersections with new fragments
            // which are appended to the end when an existing fragment is split.
            existingFragmentsCount = fragments.size();
            for (int existingFragmentIndex = 0; existingFragmentIndex < existingFragmentsCount; existingFragmentIndex++) {
                // Since lines cannot intersect at multiple places
                // do not check for additional intersections with new fragment candidates
                // which are appended to the end when an fragment candidate is split.
                candidateCount = fragmentCandidates.size();
                for (int candidateIndex = 0; candidateIndex < candidateCount; candidateIndex++) {
                    // Check for intersection, edges inclusive to detect T junctions.
                    intersection = fragments.get(existingFragmentIndex).intersection(line, true);
                    if (intersection != null) {
                        //Split existing fragment if its edge is not part of a T or V junction.
                        existingFragment = fragments.get(existingFragmentIndex);
                        if (!(intersection.equals(existingFragment.start) || intersection.equals(existingFragment.end))) {
                            fragments.add(new Line(intersection, existingFragment.end));
                            fragments.set(existingFragmentIndex, new Line(existingFragment.start, intersection));
                        }
                        // Split fragment candidate if its edge is not part of a T or V junction.
                        fragmentCandidate = fragmentCandidates.get(candidateIndex);
                        if (!(intersection.equals(fragmentCandidate.start) || intersection.equals(fragmentCandidate.end))) {
                            fragmentCandidates.add(new Line(intersection, fragmentCandidate.end));
                            fragmentCandidates.set(candidateIndex, new Line(fragmentCandidate.start, intersection));
                        }
                    }
                }
            }
            // Add candidates to fragments.
            for (Line fragment : fragmentCandidates) {
                fragments.add(fragment);
            }
        }
        return fragments;
    }

    /**
     * Converts a set of undirected lines to a set of nodes.
     *
     * @param lines Undirected lines.
     * @return Set of nodes each containing every line that starts or ends in it with start point inside the node.
     */
    private static List<Node> linesToNodes(Iterable<Line> lines) {
        List<Node> nodes = new ArrayList<>();
        Node startNode;
        Node endNode;
        Point currentPosition;
        for (Line line :
                lines) {
            // Find start and end node
            startNode = null;
            endNode = null;
            for (int i = 0; i < nodes.size(); i++) {
                currentPosition = nodes.get(i).position;
                if (currentPosition.equals(line.start))
                    startNode = nodes.get(i);
                if (currentPosition.equals(line.end))
                    endNode = nodes.get(i);
                if (startNode != null && endNode != null)
                    break;
            }
            // Create start node if not found.
            if (startNode == null) {
                startNode = new Node(line.start);
                nodes.add(startNode);
            }
            // Create end node if not found.
            if (endNode == null) {
                endNode = new Node(line.end);
                nodes.add(endNode);
            }
            // Connect nodes.
            startNode.link(endNode, line);
        }
//        for (Node node :
//                nodes) {
//            Log.d("Nodes", node.toString());
//        }
        return nodes;
    }

    /**
     * Convert properly generated nodes to a set of polygons.
     *
     * @param nodes Nodes containing line sides from which polygons are constructed.
     * @return Set of constructed polygons.
     */
    private static List<Polygon> nodesToPolygons(List<Node> nodes) {
        List<Polygon> polygons = new ArrayList<>();
        Node node;
        Node.Link link;
        Polygon polygon;
        boolean success;
        while (!nodes.isEmpty()) {
            polygon = new Polygon();

            // start polygon anywhere
            node = nodes.get(0);
            link = node.walkAnywhere();
            polygon.addSide(link.path);
            // remove cleared nodes
            if (node.isCleared()) {
                nodes.remove(node);
            }
            // walk left until polygon is complete
            while (!polygon.isComplete()) {
                // visit next node
                node = link.destination;
                link = node.walkLeft(link.path.line.vector);
                success = polygon.addSide(link.path);
                if (!success)
                    Log.d("polygonisation", "add side failed");
                // remove cleared nodes
                if (node.isCleared())
                    nodes.remove(node);
            }
            polygons.add(polygon);
        }
        return polygons;
    }

    /**
     * Set of line sides leaving a junction point.
     */
    private static class Node {
        /**
         * Position of junction point.
         */
        public final Point position;
        /**
         * Unconsumed set of line sides leaving the junction point.
         */
        protected final List<Link> availableLinks;

        /**
         * Creates a new junction point.
         *
         * @param point Position o the junction point.
         */
        public Node(Point point) {
            position = point;
            availableLinks = new ArrayList<>();
        }

        /**
         * Checks whether all line sides leaving the junction point are consumed.
         *
         * @return True when all line sides are consumed, false otherwise.
         */
        public boolean isCleared() {
            return availableLinks.isEmpty();
        }

        /**
         * Connects two points. Splits the line into two line sides and add them to the nodes for consumption.
         *
         * @param destination Node at which line ends.
         * @param line        Line starting from this node.
         */
        public void link(Node destination, Line line) {
            Polygon.LineSide lineSide = new Polygon.LineSide(line);
            availableLinks.add(new Link(lineSide, destination));
            destination.availableLinks.add(destination.new Link(lineSide.otherSide, this));
        }

        /**
         * Consumes the most left line side leaving this junction.
         *
         * @param from Direction from which the junction is approached.
         * @return Consumed line side leaving this junction or null if node is clear.
         */
        public Link walkLeft(Vector from) {
            // get angle from where the node is approached.
            double referenceAngle = from.getAngle();
            // turn around.
            referenceAngle += Math.PI;
            if (referenceAngle > Math.PI)
                referenceAngle -= 2 * Math.PI;

            // find closest leaving line side to the left.
            int pathIndex = -1;
            double maximalAngle = -1;
            double currentAngle;
            for (int i = 0; i < availableLinks.size(); i++) {
                currentAngle = availableLinks.get(i).path.line.vector.getAngle() - referenceAngle;
                if (currentAngle <= -0.001)// a threshold to avoid always going back the same way.
                    currentAngle += 2 * Math.PI;
                if (currentAngle > maximalAngle) {
                    maximalAngle = currentAngle;
                    pathIndex = i;
                }
            }
            if (pathIndex == -1)
                return null;
            Link path = availableLinks.get(pathIndex);
            availableLinks.remove(pathIndex);
            return path;
        }

        /**
         * Consume any line side leaving this node.
         *
         * @return consumed line side or null if node is cleared.
         */
        public Link walkAnywhere() {
            if (availableLinks.isEmpty())
                return null;
            Link path = availableLinks.get(0);
            availableLinks.remove(0);
            return path;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(position.toString());
            builder.append(" -> {");
            for (Link link :
                    availableLinks) {
                builder.append(link.path.line.end);
            }
            builder.append("}");
            return builder.toString();
        }

        /**
         * Connection from outer node instance to destination over path.
         */
        class Link {
            /**
             * Line side connecting outer node instance and destination.
             */
            public final Polygon.LineSide path;
            /**
             * Node to which path leads.
             */
            public final Node destination;

            /**
             * Creates a new link from outer node instance to destination over path.
             *
             * @param path        Line side connecting outer node instance and destination.
             * @param destination Node to which path leads.
             */
            public Link(Polygon.LineSide path, Node destination) {
                this.path = path;
                this.destination = destination;
            }
        }
    }
}
