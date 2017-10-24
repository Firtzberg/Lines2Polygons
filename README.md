# Lines2Polygons
<p>Converts a set of undirected lines within a frame to a set of polygons without holes representing the enclosed areas.</p>

# General principle
<p><ul>
<li>All undirected lines are first split at intersection points.</li>
<li>Then for each fragment two directed line sides are created.</li>
<li>Line sides with same start point belong to the same node. Line sides act as links between nodes.</li>
<li>Polygons are constructed by appending line sides. The first added line side is not removed from the nodes. A polygon is completed when trying to add the first line side once again.</li>
<li>The first line side can be an arbitrary line side from an arbitrary node. After that always take the most left (or right) line side and remove it from the nodes until a polygon is constructed.</li>
<li>Construct polygons until all line sides are removed.</li>
</ul></p>

# Android app

<p>The implemented algorithm can be observed in an Android app which can be downloaded <a href="https://play.google.com/store/apps/details?id=com.firtzberg.lines2polygons">here</a>.
Feel free to observe the algorithm in action.</p>
<p>OpenGL ES is used to render the resulting polygons which rotate in 3D.
In order to draw the polygons they need to be cut into triangles. The ear clipping algorithm is used.</p>
<p>The grid size is 10x10 and only lines with points with integer values can be entered.</p>

# Known issues
<p>Except the polygons representing the enclosed areas inside the frame
a polygon spanning over the frame in the opposite traversal direction is constructed
representing the area outside the frame.
</p>
<p>A set of lines not connected to the frame will in addition to the polygons representing the internal areas
also generate a polygon with opposite traversal direction representing the boundaries of the separate line group.</p>
<p>The polygon triangulation fails when the area spanned by several adjacent line sides is 0.</p>
