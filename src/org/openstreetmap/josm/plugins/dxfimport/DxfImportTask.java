// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.dxfimport;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.data.projection.ProjectionRegistry;
import org.openstreetmap.josm.data.projection.Projections;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.I18n;
import org.openstreetmap.josm.tools.Logging;

import com.kitfox.svg.Group;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGUniverse;
import com.kitfox.svg.ShapeElement;

/**
 * This class allows us to import data in josm, from a file
 * @author adrian_antochi
 */
public class DxfImportTask extends PleaseWaitRunnable {
    LinkedList<Node> nodes = new LinkedList<>();
    LinkedList<Way> ways = new LinkedList<>();
    private File file;
    private boolean canceled;

    Projection projection = Projections.getProjectionByCode("EPSG:3857"); // Mercator
    EastNorth center;
    double scale;
    Way currentway;
    double lastX;
    double lastY;

    SequenceCommand resultCommand;

    public DxfImportTask(File file) {
        super(I18n.tr("Importing..."), false);
        this.file = file;
    }

    @Override
    protected void cancel() {
        this.canceled = true;
    }

    @Override
    protected void finish() {
    }

    private void appendNode(double x, double y) throws IOException {
        if (currentway == null) {
            throw new IOException(tr("Shape is started incorrectly"));
        }
        Node nd = new Node(projection.eastNorth2latlon(center.add(x * scale, -y * scale)));
        if (nd.getCoor().isOutSideWorld()) {
            Logging.error("Shape goes outside the world: " + nd.getCoor());
        } else {
            currentway.addNode(nd);
            nodes.add(nd);
            lastX = x;
            lastY = y;
        }
    }

    private void appendNode(Point2D point) throws IOException {
        appendNode(point.getX(), point.getY());
    }

    private static double sqr(double x) {
        return x * x;
    }

    private static double cube(double x) {
        return x * x * x;
    }

    private static Point2D interpolateQuad(double ax, double ay, double bx, double by, double cx, double cy, double t) {
        return new Point2D.Double(
                sqr(1 - t) * ax + 2 * (1 - t) * t * bx + t * t * cx, 
                sqr(1 - t) * ay + 2 * (1 - t) * t * by + t * t * cy);
    }

    private static Point2D interpolateCubic(double ax, double ay, double bx, double by, double cx, double cy, double dx, double dy, double t) {
        return new Point2D.Double(
                cube(1 - t) * ax + 3 * sqr(1 - t) * t * bx + 3 * (1 - t) * t * t * cx + t * t * t * dx, 
                cube(1 - t) * ay + 3 * sqr(1 - t) * t * by + 3 * (1 - t) * t * t * cy + t * t * t * dy);
    }

    private void processElement(SVGElement el, AffineTransform transform) throws IOException {
        if (el instanceof Group) {
            AffineTransform oldTransform = transform;
            AffineTransform xform = ((Group) el).getXForm();
            if (transform == null) {
                transform = xform;
            } else if (xform != null) {
                transform = new AffineTransform(transform);
                transform.concatenate(xform);
            }
            for (Object child : ((Group) el).getChildren(null)) {
                processElement((SVGElement) child, transform);
            }
            transform = oldTransform;
        } else if (el instanceof ShapeElement) {
            Shape shape = ((ShapeElement) el).getShape();
            if (transform != null) {
                shape = transform.createTransformedShape(shape);
            }
            PathIterator it = shape.getPathIterator(null);
            while (!it.isDone()) {
                double[] coords = new double[6];
                switch (it.currentSegment(coords)) {
                    case PathIterator.SEG_MOVETO:
                        currentway = new Way();
                        ways.add(currentway);
                        appendNode(coords[0], coords[1]);
                        break;
                    case PathIterator.SEG_LINETO:
                        appendNode(coords[0], coords[1]);
                        break;
                    case PathIterator.SEG_CLOSE:
                        if (currentway.firstNode().getCoor().equalsEpsilon(nodes.getLast().getCoor())) {
                            currentway.removeNode(nodes.removeLast());
                        }
                        currentway.addNode(currentway.firstNode());
                        break;
                    case PathIterator.SEG_QUADTO:
                        double lastx = lastX;
                        double lasty = lastY;
                        for (int i = 1; i < Settings.getCurveSteps(); i++) {
                            appendNode(interpolateQuad(lastx, lasty, coords[0], coords[1], coords[2], coords[3], i / Settings.getCurveSteps()));
                        }
                        appendNode(coords[2], coords[3]);
                        break;
                    case PathIterator.SEG_CUBICTO:
                        lastx = lastX;
                        lasty = lastY;
                        for (int i = 1; i < Settings.getCurveSteps(); i++) {
                            appendNode(interpolateCubic(lastx, lasty, coords[0], coords[1], coords[2], coords[3], coords[4], coords[5], 
                                    i / Settings.getCurveSteps()));
                        }
                        appendNode(coords[4], coords[5]);
                        break;
                }
                it.next();
            }
        }
    }

    @Override
    protected void realRun() throws IOException, OsmTransferException {
        LatLon center = ProjectionRegistry.getProjection().eastNorth2latlon(MainApplication.getMap().mapView.getCenter());
        scale = Settings.getScaleNumerator() / Settings.getScaleDivisor() / Math.cos(Math.toRadians(center.lat()));
        this.center = projection.latlon2eastNorth(center);
        try {
            SVGUniverse universe = new SVGUniverse();
            universe.setVerbose(Config.getPref().getBoolean("importdxf.verbose", false));
            if (canceled) {
                return;
            }
            Path tempPath = Files.createTempFile("importTaskTemp", ".dxf"); // creating a temp file for generated svg

            processUsingKabeja(file, tempPath.toString());

            SVGDiagram diagram = universe.getDiagram(tempPath.toUri()); // this is where the rest of the conversion happens
            if (diagram == null) {
                Logging.error("Unable to load SVG diagram for {0}", tempPath.toUri());
                displayError(tr("Can''t load SVG diagram"));
                return;
            }
            //if there's no access to the temp file, thing breaks down
            ShapeElement root = diagram.getRoot();
            if (root == null) {
                Logging.error("Unable to load SVG diagram for {0}", tempPath.toUri());
                displayError(tr("Can''t find root SVG element"));
                return;
            }
            Rectangle2D bbox = root.getBoundingBox();
            this.center = this.center.add(-bbox.getCenterX() * scale, bbox.getCenterY() * scale);
            processElement(root, null);
        } catch (IOException e) {
            Logging.error(e);
            throw e;
        } catch (Exception | LinkageError e) {
            Logging.error(e);
            throw new IOException(e);
        }
        DataSet ds = MainApplication.getLayerManager().getEditDataSet();
        List<Command> cmds = new ArrayList<>();
        for (Node n : nodes) {
            cmds.add(new AddCommand(ds, n));
        }
        for (Way w : ways) {
            if (w.getNodesCount() > 0) {
                cmds.add(new AddCommand(ds, w));
            }
        }
        if (!cmds.isEmpty()) {
            resultCommand = new SequenceCommand(tr("Import primitives"), cmds);
            GuiHelper.runInEDTAndWait(() -> UndoRedoHandler.getInstance().add(resultCommand));
        }
    }

    private static void displayError(String error) {
        GuiHelper.runInEDT(() -> new Notification(error).setIcon(JOptionPane.ERROR_MESSAGE).show());
    }

    public static void processUsingKabeja(File file, String tempFile) throws IOException {
        Logging.debug("Initializing Kabeja");
        org.kabeja.Main kabeja = new org.kabeja.Main();
        try (InputStream conf = DxfImportPlugin.class.getResourceAsStream("/resources/process.xml")) {
            if (conf == null) {
                throw new IOException(tr("Cannot find configuration file!"));
            }
            kabeja.setProcessConfig(conf);
        }
        kabeja.setSourceFile(file.getAbsolutePath());
        kabeja.setDestinationFile(tempFile); // temp file, where the .svg will be stored
        kabeja.setPipeline("svg"); // converting to svg
        kabeja.omitUI(true); // we don't need no ui
        kabeja.initialize();
        Logging.debug("Kabeja initialized. Processing...");
        kabeja.process(); // this is where some of the conversion happens
        Logging.debug("Kabeja processing done");
    }
}
