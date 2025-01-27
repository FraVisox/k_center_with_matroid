package datasetUtils;
import it.unidp.dei.Point;
import it.unidp.dei.TestUtils;
import it.unidp.dei.datasetReaders.CovertypeReader;
import it.unidp.dei.datasetReaders.DatasetReader;
import it.unidp.dei.datasetReaders.HiggsReader;
import it.unidp.dei.datasetReaders.PhonesReader;

import java.io.*;
import java.util.*;

// This is the translation in Java of Algorithm 1 of https://github.com/Krrithen/DoublingDimension
public class DoublingDimension {
    private static final String[] datasets = {"Phones_accelerometer.csv", "covtype.dat", "HIGGS.csv"};
    private static final String[] outFiles = {"DoublDim_Phones.txt", "DoublDim_Covtype.txt", "DoublDim_Higgs.txt"};
    private static final Class[] readers = {PhonesReader.class, CovertypeReader.class, HiggsReader.class};

    private static final String outFolder = TestUtils.outFolder;
    private static final String inFolder = TestUtils.inFolderRandomized;
    public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException {

        for (int i = 2; i>0; i--) {
            // Read graph from file
            DatasetReader reader = (DatasetReader) readers[i].newInstance();
            if (i != 2) {
                reader.setSource(inFolder + datasets[i]);
            } else {
                reader.setSource(TestUtils.inFolderOriginals + datasets[i]);
            }
            List<Node> nodes = readGraphFromCSV(reader);

            PrintWriter writer = new PrintWriter(outFolder + outFiles[i]);


            // Compute the doubling dimension
            Node bestStartNode = null;
            double bestDoublingDimension = Double.MAX_VALUE;

            int index = 0;
            for (Node startNode : nodes) {
                CoverTree coverTree = new CoverTree(nodes);
                Map<Integer, List<Node>> tree = coverTree.buildCoverTree(startNode);

                double maxDoublingConstant = 0;

                for (Map.Entry<Integer, List<Node>> entry : tree.entrySet()) {
                    int level = entry.getKey();
                    List<Node> parents = entry.getValue();
                    double doublingConstant = 0;

                    for (Node parent : parents) {
                        doublingConstant = Math.max(doublingConstant,
                                tree.getOrDefault(level + 1, new ArrayList<>()).stream()
                                        .filter(child -> parent.getDistance(child) <= Math.pow(2, level))
                                        .count());
                    }

                    maxDoublingConstant = Math.max(maxDoublingConstant, doublingConstant);
                }

                double doublingDimension = Math.log(maxDoublingConstant) / Math.log(2);

                if (doublingDimension < bestDoublingDimension) {
                    bestDoublingDimension = doublingDimension;
                    bestStartNode = startNode;
                }

                index++;
                if (index % 500 == 0) {
                    System.out.println(index);
                }
            }

            System.out.println("Best starting node: " + Arrays.toString(bestStartNode.getCoordinates()));
            System.out.println("Best doubling dimension: " + bestDoublingDimension);
            System.out.println("END OF "+datasets[i]);
            writer.write("Best starting node: " + Arrays.toString(bestStartNode.getCoordinates()));
            writer.write("Best doubling dimension: " + bestDoublingDimension);
            writer.close();
        }
    }

    private static List<Node> readGraphFromCSV(DatasetReader reader) {
        //Compute the doubling dimension only of the window
        List<Node> nodes = new ArrayList<>();

        int time = 1; //TODO: make of the whole dataset
        while (reader.hasNext() && time <= 10200) {
            Point p = reader.nextPoint(0, 0);
            nodes.add(new Node(p));
            time++;
        }

        reader.close();
        return nodes;
    }
}




class CoverTree {
    private final List<Node> nodes;
    private final Map<Node, List<Node>> parentChildMap;

    // Constructor
    public CoverTree(List<Node> nodes) {
        this.nodes = nodes;
        this.parentChildMap = new HashMap<>();
    }

    // Build the hierarchical cover tree
    public Map<Integer, List<Node>> buildCoverTree(Node startNode) {
        Map<Integer, List<Node>> levels = new HashMap<>();
        double radius = calculateInitialRadius();
        int level = 0;

        while (radius >= 1) {
            Set<Node> uncoveredNodes = new HashSet<>(nodes);
            List<Node> centers = new ArrayList<>();

            if (startNode != null && uncoveredNodes.contains(startNode)) {
                centers.add(startNode);
                uncoveredNodes.remove(startNode);
                double finalRadius = radius;
                uncoveredNodes.removeIf(n -> startNode.getDistance(n) <= finalRadius);
            }

            while (!uncoveredNodes.isEmpty()) {
                Node center = uncoveredNodes.iterator().next();
                centers.add(center);
                uncoveredNodes.remove(center);
                double finalRadius1 = radius;
                uncoveredNodes.removeIf(n -> center.getDistance(n) <= finalRadius1);
            }

            levels.put(level, centers);

            if (level > 0) {
                for (Node child : centers) {
                    List<Node> parents = new ArrayList<>();
                    for (Node parent : levels.get(level - 1)) {
                        if (parent.getDistance(child) <= 2 * radius) {
                            parents.add(parent);
                        }
                    }
                    parentChildMap.put(child, parents);
                }
            }

            radius /= 2;
            level++;
        }

        return levels;
    }

    private double calculateInitialRadius() {
        double maxDistance = 0;
        for (Node u : nodes) {
            for (Node v : nodes) {
                if (!u.equals(v)) {
                    maxDistance = Math.max(maxDistance, u.getDistance(v));
                }
            }
        }
        return maxDistance;
    }

    public List<Edge> getEdges() {
        List<Edge> edges = new ArrayList<>();
        for (Map.Entry<Node, List<Node>> entry : parentChildMap.entrySet()) {
            Node child = entry.getKey();
            for (Node parent : entry.getValue()) {
                edges.add(new Edge(parent, child));
            }
        }
        return edges;
    }
}

class Node {
    private final Point p;

    public Node(Point p) {
        this.p = p;
    }

    public double[] getCoordinates() {
        return p.getCoordinates();
    }

    public double getDistance(Node n) {
        return p.getDistance(n.p);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Arrays.equals(p.getCoordinates(), node.p.getCoordinates());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(p.getCoordinates());
    }
}

class Edge {
    private final Node from;
    private final Node to;

    public Edge(Node from, Node to) {
        this.from = from;
        this.to = to;
    }

    public Node getFrom() {
        return from;
    }

    public Node getTo() {
        return to;
    }
}
