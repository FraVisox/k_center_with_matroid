package it.unidp.dei;

import org.jgrapht.alg.flow.PushRelabelMFImpl;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.util.*;

public class CHEN implements Algorithm {

    public CHEN(int[] _ki) {
        pts = new LinkedList<>();
        ki = _ki;
    }

    public CHEN (LinkedList<Point> p, int[] _ki) {
        pts = p;
        ki = _ki;
    }

    public void update(Point p, int time) {
        if (!pts.isEmpty() && pts.getFirst().hasExpired(time)) {
            pts.removeFirst();
        }
        pts.add(p);
    }

    public ArrayList<Point> query() {
        //TODO: test this algorithm

        TreeSet<Double> distances = new TreeSet<>();
        for (Point p : pts) {
            for (Point q : pts) {
                //By doing this, we delete all the zero distances
                if (!q.equals(p)) {
                    distances.add(p.getDistance(q));
                }
            }
        }

        //They are naturally in ascending order
        for (double dist : distances) {
            ArrayList<Point> sol = queryDist(dist);
            if (!(sol == null)) {
                return sol;
            }
        }

        //This happens only if we have only one point. In all the other cases, a valid
        //non-zero distance is found
        return new ArrayList<>(pts);
    }

    public int getSize() {
        return pts.size();
    }

    private ArrayList<Point> queryDist(double dist) {
        //Create the partition: the point in the map is the head of the partition
        TreeMap<Point, ArrayList<Point>> partition = new TreeMap<>();

        //First point is inserted
        partition.put(pts.getFirst(), new ArrayList<>());

        //Then every other point
        for (Point p : pts) {
            double minD = p.getMinDistance(partition.keySet());
            if (minD > 2*dist) {
                partition.put(p, new ArrayList<>());
            }
        }

        //Then we create all the partitions
        for (Point p : pts) {
            for (Point pivot : partition.keySet()) {
                if (p.getDistance(pivot) <= dist) {
                    partition.get(pivot).add(p);
                }
            }
        }

        return partitionMatroidIntersection(partition);
    }

    //The idea is taken from CHIPLUNKAR ET AL.
    private ArrayList<Point> partitionMatroidIntersection(TreeMap<Point, ArrayList<Point>> partition) {
        //Create a directed weighted graph
        SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>  graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);

        //Add the source vertex
        graph.addVertex("s");

        for (Point pivot : partition.keySet()) {
            //Add a vertex l+exitTime for every head of every partition
            graph.addVertex("l"+pivot.toString());

            //Connect every pivot with the source, capacity 1
            DefaultWeightedEdge e = graph.addEdge("s", "l"+pivot.toString());
            graph.setEdgeWeight(e, 1);

            for (Point p : partition.get(pivot)) {

                //Add a vertex m+exitTime for every point
                graph.addVertex("m"+p.toString());
                //Add a vertex r+indexOfGroup for every group
                graph.addVertex("r"+Integer.toString(p.getGroup()));

                //Connect every head of partition with its points, capacity 1
                e = graph.addEdge("l"+pivot.toString(), "m"+p.toString());
                graph.setEdgeWeight(e, 1);

                //Connect every point with its group, capacity 1
                e = graph.addEdge("m"+p.toString(), "r"+Integer.toString(p.getGroup()));
                graph.setEdgeWeight(e, 1);
            }
        }

        //Add sink vertex
        graph.addVertex("t");

        for (int i = 0; i < ki.length; i++) {
            //Add all the remaining vertices for the other groups
            graph.addVertex("r"+Integer.toString(i));

            //Connect the groups with the sink, capacity ki of that group
            DefaultWeightedEdge e = graph.addEdge("r"+i, "t");
            graph.setEdgeWeight(e, ki[i]);
        }

        //Use the PushRelabel algorithm to calculate the flow from source to sink
        PushRelabelMFImpl<String, DefaultWeightedEdge> alg = new PushRelabelMFImpl<>(graph);
        double flow = alg.calculateMaximumFlow("s", "t");

        //If the flow is less than the number of partitions
        if (flow != partition.keySet().size()) {
            return null;
        }

        //Get the values of flow through each edge
        Map<DefaultWeightedEdge, Double> flows = alg.getFlowMap();

        ArrayList<Point> centers = new ArrayList<>(partition.keySet().size());

        //For every point of the partition (which contains every point of pts, so we can simplify by searching only in pts)
        //if the flow value of the edge that connects it to its group is 1, we add it to the centers
        for (Point p : pts) {
            DefaultWeightedEdge edge = (graph.getEdge("m"+p, "r"+p.getGroup()));
            if (flows.get(edge) == 1) {
                centers.add(p);
            }
        }
        return centers;
    }

    private final LinkedList<Point> pts;
    private final int[] ki;
}
