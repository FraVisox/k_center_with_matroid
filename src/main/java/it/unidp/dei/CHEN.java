package it.unidp.dei;

import org.jgrapht.Graphs;
import org.jgrapht.alg.flow.PushRelabelMFImpl;
import org.jgrapht.alg.util.Triple;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.*;
import org.jgrapht.opt.graph.sparse.SparseIntDirectedWeightedGraph;

//TODO: improvements
public class CHEN implements Algorithm {
    public CHEN(int[] _ki) {
        pts = new LinkedList<>();
        ki = _ki;
        int tmp = 0;
        for (int kj : ki) {
            tmp += kj;
        }
        k = tmp;
    }

    public CHEN (LinkedList<Point> p, int[] _ki) {
        pts = p;
        ki = _ki;
        int tmp = 0;
        for (int kj : ki) {
            tmp += kj;
        }
        k = tmp;
    }

    public void update(Point p, int time) {
        if (!pts.isEmpty() && pts.getFirst().hasExpired(time)) {
            pts.removeFirst();
        }
        pts.addLast(p);
    }

    public ArrayList<Point> query() {

        //First we create all the distances and put them in an array
        double[] distances = new double[pts.size()*pts.size()-pts.size()];
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

    public int getSize() {
        return pts.size();
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
        //TODO: meglio sparse o no?
        return sparsePartitionMatroidIntersection(partition);
    }

    //The idea is taken from CHIPLUNKAR ET AL.
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

    private ArrayList<Point> sparsePartitionMatroidIntersection(TreeMap<Point, ArrayList<Point>> partition) {

        List<Triple<Integer, Integer, Double>> edges = new ArrayList<>(partition.size());
        //Node 0 is the source, node 1 to group.size() are the group nodes, nodes partition.size()+1 to
        int i = ki.length+1;
        for (Point pivot : partition.keySet()) {
            edges.add(new Triple<>(0, i, (double)1));
            int i_pivot = i;
            i++;
            for (Point p : partition.get(pivot)) {
                edges.add(new Triple<>(i_pivot, i, (double)1));
                edges.add(new Triple<>(i, p.getGroup()+1, (double)1));
                i++;
            }
        }
        //Now i is the number of the sink
        for (int j = 0; j<ki.length; j++) {
            edges.add(new Triple<>(j+1, i, (double)ki[j]));
        }

        SparseIntDirectedWeightedGraph graph = new SparseIntDirectedWeightedGraph(i+1, edges);

        //Use the PushRelabel algorithm to calculate the flow from source to sink. It's the most efficient algorithm
        PushRelabelMFImpl<Integer, Integer> alg = new PushRelabelMFImpl<>(graph);
        double flow = alg.calculateMaximumFlow(0, i);

        //If the flow is less than the number of partitions, it's a failure
        if (flow != partition.keySet().size()) {
            return null;
        }

        //Get the values of flow through each edge
        Map<Integer, Double> flows = alg.getFlowMap();

        ArrayList<Point> centers = new ArrayList<>((int)flow);

        //For every point of the partition if the flow value of the edge that connects it to its group is 1, we add it to the centers
        //The points of the partition are not all the points that are in pts
        int new_i = ki.length+1;
        for (Point pivot : partition.keySet()) {
            new_i++;
            ArrayList<Point> thisPartition = partition.get(pivot);
            for (int z = 0; z<thisPartition.size(); z++) {
                Point p = thisPartition.get(z);
                Integer edge = graph.getEdge(new_i, p.getGroup()+1);
                if (flows.get(edge) == 1) {
                    centers.add(p);

                    //TODO: testa se è corretto
                    new_i += thisPartition.size()-z;
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
