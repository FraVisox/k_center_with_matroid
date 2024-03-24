package it.unidp.dei.CHENETAL;

import it.unidp.dei.Algorithm;
import it.unidp.dei.Point;
import org.jgrapht.alg.flow.PushRelabelMFImpl;
import org.jgrapht.alg.util.Triple;

import java.util.*;
import org.jgrapht.opt.graph.sparse.SparseIntDirectedWeightedGraph;

public class CHEN implements Algorithm {

    //Approximation of sequential algorithm. In our case, CHEN gives a 3-approximation
    public static final int alfa = 3;

    public CHEN(int[] _ki) {
        pts = new LinkedList<>();
        ki = _ki;
        k = Algorithm.calcK(_ki);
    }

    public CHEN (LinkedList<Point> p, int[] _ki) {
        pts = p;
        ki = _ki;
        k = Algorithm.calcK(_ki);
    }

    @Override
    public void update(Point p, int time) {
        if (!pts.isEmpty() && pts.getFirst().hasExpired(time)) {
            pts.removeFirst();
        }
        pts.addLast(p);
    }

    @Override
    public ArrayList<Point> query() {

        //First we create all the distances and put them in an array
        int length = pts.size()*pts.size()-pts.size();
        if (pts.size() <= k) {
            length++;
        }
        double[] distances = new double[length];
        int i = 0;
        for (Point p : pts) {
            for (Point q : pts) {
                //By doing this, we delete all the distances of one point from itself (that are zeroes)
                if (!q.equals(p)) {
                    distances[i] = p.getDistance(q);
                    i++;
                }
            }
        }
        if (pts.size() <= k) {
            distances[i] = 0;
        }

        //Then we sort all the distances, so that they are in non decreasing order
        Arrays.sort(distances);

        //Then we perform a binary search on the distances to search the best answer
        ArrayList<Point> sol = new ArrayList<>(pts);
        int low = 0;
        int high = distances.length-1;
        while (low <= high) {
            int mid = (high + low) / 2;

            //Distance 0 is optimal only if we have less than k points
            if (distances[mid] == 0 && pts.size() > k) {
                low = mid + 1;
            }

            //We try to obtain k centers with that distance as the radius
            ArrayList<Point> thisSol = queryDist(distances[mid]);
            if (thisSol != null) {
                sol = thisSol;
                high = mid - 1;
            } else  {
                low = mid + 1;
            }
        }
        return sol;
    }

    @Override
    public int getSize() {
        return pts.size();
    }

    public LinkedList<Point> getPoints() {
        return pts;
    }

    //It returns null if the distance is not suitable to create a k-center clustering
    private ArrayList<Point> queryDist(double dist) {

        //Create the partition: the key point in the map is the head of the partition
        TreeMap<Point, ArrayList<Point>> partition = new TreeMap<>();

        //We take a random point (the first one to simplify)
        partition.put(pts.getFirst(), new ArrayList<>());

        //Then every other point
        for (int i = 1; i< pts.size(); i++) {
            Point p = pts.get(i);
            double minD = p.getMinDistance(partition.keySet());
            //If its distance from the set of centers is more than 2*dist, it becomes another center
            if (minD > 2*dist) {
                partition.put(p, new ArrayList<>());

                //If the number of centers is more than k, we already know that this won't be a valid guess
                if (partition.keySet().size() > k) {
                    return null;
                }
            }
        }

        //Then we create all the partitions
        for (Point p : pts) {
            for (Point pivot : partition.keySet()) {
                if (p.getDistance(pivot) <= dist) {
                    //We know that every point can be at distance <= dist from only one point of pivots
                    //The demonstration is in the paper by Chen et al.
                    partition.get(pivot).add(p);
                    break;
                }
            }
        }

        //Then we resolve the partition matroid intersection
        return sparsePartitionMatroidIntersection(partition);
    }

    //We use a sparse graph, as the graph isn't changed after its creation, but only used to compute the maximum flow
    private ArrayList<Point> sparsePartitionMatroidIntersection(TreeMap<Point, ArrayList<Point>> partition) {

        List<Triple<Integer, Integer, Double>> edges = new ArrayList<>(partition.size());
        //MAPPING OF NODES TO INTEGER (essential for the sparse graph):
        // node 0 is the source
        // nodes 1 to group.size() are the group nodes
        // node with the last number is the sink
        // all the nodes in the middle are both partition pivots and partition points:
        //      for every partition, the first point is the pivot, while all the remaining are points inside the partition

        //This is how edges are created: there is an edge between:
        //  source and pivots
        //  pivots and points in their partition
        //  points and their group
        //  groups and the sink
        int i = ki.length + 1;
        for (Point pivot : partition.keySet()) {
            edges.add(new Triple<>(0, i, (double) 1));
            int i_pivot = i;
            i++;
            for (Point p : partition.get(pivot)) {
                edges.add(new Triple<>(i_pivot, i, (double) 1));
                edges.add(new Triple<>(i, p.getGroup() + 1, (double) 1));
                i++;
            }
        }
        //Now i is the number of the sink
        for (int j = 0; j < ki.length; j++) {
            edges.add(new Triple<>(j + 1, i, (double) ki[j]));
        }

        //Create graph
        SparseIntDirectedWeightedGraph graph = new SparseIntDirectedWeightedGraph(i + 1, edges);

        //Use the PushRelabel algorithm to calculate the flow from source to sink. It's the most efficient algorithm
        PushRelabelMFImpl<Integer, Integer> alg = new PushRelabelMFImpl<>(graph);
        double flow = alg.calculateMaximumFlow(0, i);

        //If the flow is less than the number of partitions, it's a failure
        if (flow != partition.keySet().size()) {
            return null;
        }

        //Get the values of flow through each edge
        Map<Integer, Double> flows = alg.getFlowMap();
        ArrayList<Point> centers = new ArrayList<>((int) flow);

        //For every point of the partition if the flow value of the edge that connects it to its group is 1, we add it to the centers
        //The points of the partition are not all the points that are in pts
        int new_i = ki.length + 1;
        for (Point pivot : partition.keySet()) {
            new_i++;
            ArrayList<Point> thisPartition = partition.get(pivot);
            for (int z = 0; z < thisPartition.size(); z++) {
                Point p = thisPartition.get(z);
                Integer edge = graph.getEdge(new_i, p.getGroup() + 1);
                if (flows.get(edge) == 1) {
                    centers.add(p);
                    new_i += thisPartition.size() - z;
                    break;
                }
                new_i++;
            }
        }
        return centers;
    }
    private final LinkedList<Point> pts;
    private final int[] ki;
    private final int k;
}

/*
ALTERNATIVE to the sparse matroid intersection that uses a default graph

    private ArrayList<Point> partitionMatroidIntersection(TreeMap<Point, ArrayList<Point>> partition) {

        //Create a directed weighted graph
        DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);

        for (Point pivot : partition.keySet()) {

            Graphs.addEdgeWithVertices(graph, "source", "pivot"+pivot.toString(), 1);

            for (Point p : partition.get(pivot)) {
                Graphs.addEdgeWithVertices(graph, "pivot"+pivot.toString(), "point"+p.toString(), 1);
                Graphs.addEdgeWithVertices(graph, "point"+p.toString(), "group"+p.getGroup(), 1);
            }
        }

        for (int i = 0; i < ki.length; i++) {
            Graphs.addEdgeWithVertices(graph, "group"+i, "sink", ki[i]);
        }

        //Use the PushRelabel algorithm to calculate the flow from source to sink. It's the most efficient algorithm
        PushRelabelMFImpl<String, DefaultWeightedEdge> alg = new PushRelabelMFImpl<>(graph);
        double flow = alg.calculateMaximumFlow("source", "sink");

        //If the flow is less than the number of partitions
        if (flow != partition.keySet().size()) {
            return null;
        }

        //Get the values of flow through each edge
        Map<DefaultWeightedEdge, Double> flows = alg.getFlowMap();

        ArrayList<Point> centers = new ArrayList<>((int)flow);

        //For every point of the partition if the flow value of the edge that connects it to its group is 1, we add it to the centers
        //The points of the partition are not all the points that are in pts
        for (Point pivot : partition.keySet()) {
            for (Point p : partition.get(pivot)) {
                DefaultWeightedEdge edge = graph.getEdge("point" + p, "group" + p.getGroup());
                if (flows.get(edge) == 1) {
                    centers.add(p);
                    //Pass to the next partition
                    break;
                }
            }
        }
        return centers;
    }
     */
