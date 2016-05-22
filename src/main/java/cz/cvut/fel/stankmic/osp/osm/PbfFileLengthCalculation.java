package cz.cvut.fel.stankmic.osp.osm;

import de.topobyte.osm4j.core.model.iface.*;
import de.topobyte.osm4j.pbf.seq.PbfIterator;

import java.io.*;
import java.util.*;

public class PbfFileLengthCalculation {

    private static final int WAYS_INITIAL_SIZE = 20_000;

    private final String fileName;

    public PbfFileLengthCalculation(String fileName) {
        this.fileName = fileName;
    }

    private InputStream openFile() throws FileNotFoundException {
        System.out.println("Opening file.");
        return new FileInputStream(fileName);
    }

    public double getLengthOfWays(String key, String value) throws IOException {
        Collection<OsmWay> ways;
        Set<Long> nodeIds = new TreeSet<>();
        try(InputStream is = this.openFile()) {
            ways = loadWays(is, key, value, nodeIds);
            System.out.printf("Loaded %1$d ways.\n", ways.size());
        }

        Map<Long, OsmNode> nodes = null;
        try(InputStream is = this.openFile()) {
            nodes = loadNodes(is, nodeIds);
            System.out.printf("Loaded %1$d nodes.\n", nodes.size());
        } finally {
            nodeIds.clear();
        }

        double length = 0;
        for(OsmWay way : ways) {
            length += wayLength(way, nodes);
        }
        return length;
    }

    private Collection<OsmWay> loadWays(InputStream is, String key, String value, Set<Long> nodeIds) {
        Collection<OsmWay> ways = new ArrayList<>(WAYS_INITIAL_SIZE);
        Iterable<EntityContainer> containers = new PbfIterator(is, false);
        for(EntityContainer container : containers) {
            if(isWay(container) && hasTag(container, key, value)) {
                OsmWay way = (OsmWay) container.getEntity();
                ways.add(way);
                int numberOfNodes = way.getNumberOfNodes();
                for(int i=0; i<numberOfNodes; i++) {
                    nodeIds.add(way.getNodeId(i));
                }
            }
        }
        return ways;
    }

    private boolean isWay(EntityContainer container) {
        return isType(container, EntityType.Way);
    }

    private boolean isType(EntityContainer container, EntityType type) {
        return container.getType().equals(type);
    }

    private boolean hasTag(EntityContainer container, String key, String value) {
        return hasTag(container.getEntity(), key, value);
    }

    private boolean hasTag(OsmEntity entity, String key, String value) {
        int numberOfTags = entity.getNumberOfTags();
        for(int i=0; i<numberOfTags; i++) {
            OsmTag tag = entity.getTag(i);
            if(tag.getKey().equals(key) && tag.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    private Map<Long, OsmNode> loadNodes(InputStream is, Collection<Long> nodeIds) {
        Map<Long, OsmNode> nodes = new HashMap<>(nodeIds.size());
        Iterable<EntityContainer> containers = new PbfIterator(is, false);
        for(EntityContainer container : containers) {
            if(isNode(container) && nodeIds.contains(container.getEntity().getId())) {
                nodes.put(container.getEntity().getId(), (OsmNode) container.getEntity());
            }
        }
        return nodes;
    }

    private boolean isNode(EntityContainer container) {
        return isType(container, EntityType.Node);
    }

    private double wayLength(OsmWay way, Map<Long, OsmNode> nodes) {
        double length = 0;
        int numberOfNodes = way.getNumberOfNodes();
        for(int i=0; i<numberOfNodes-1; i++) {
            length += distanceForNodes(nodes.get(way.getNodeId(i)), nodes.get(way.getNodeId(i+1)));
        }
        return length;
    }

    private double distanceForNodes(OsmNode node1, OsmNode node2) {
        return Distance.getDistance(
                node1.getLatitude(),
                node1.getLongitude(),
                node2.getLatitude(),
                node2.getLongitude()
        );
    }

}
