package it.unidp.dei;

import org.jgrapht.Graphs;
import org.jgrapht.alg.flow.PushRelabelMFImpl;
import org.jgrapht.alg.util.Triple;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.opt.graph.sparse.SparseIntDirectedWeightedGraph;

import java.util.*;

public class CHENWithK implements Algorithm {

    public CHENWithK(int[] _ki) {
        pts = new LinkedList<>();
        ki = _ki;
        int tmp = 0;
        for (int kj : ki) {
            tmp += kj;
        }
        k = tmp;
    }

    public CHENWithK(LinkedList<Point> p, int[] _ki) {
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

        Arrays.sort(distances);

        //Binary search on distances
        ArrayList<Point> sol = new ArrayList<>(pts);
        int low = 0;
        int high = distances.length-1;
        while (low <= high) {
            int mid = (high + low) / 2;
            if (distances[mid] == 0) {
                low = mid + 1;
            }
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
        //Create the partition: the point in the map is the head of the partition
        TreeMap<Point, ArrayList<Point>> partition = new TreeMap<>();

        //First point is inserted
        partition.put(pts.getFirst(), new ArrayList<>());

        //Then every other point
        for (Point p : pts) {
            double minD = p.getMinDistance(partition.keySet());
            if (minD > 2*dist) {
                partition.put(p, new ArrayList<>());
                if (partition.keySet().size() > k) {
                    return null;
                }
            }
        }

        //Then we create all the partitions
        for (Point p : pts) {
            for (Point pivot : partition.keySet()) {
                if (p.getDistance(pivot) <= dist) {
                    partition.get(pivot).add(p);
                    //We already know that every point can be at distance <= dist from only one point of pivots
                    break;
                }
            }
        }
        //TODO: meglio sparse o no?
        return partitionMatroidIntersectionSPARSE(partition);
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

        //Now we can have less than k centers
        while (centers.size() < k && pointAtMaxDistanceBetweenSets(pts, centers)) {
        }

        return centers;
    }

    private boolean pointAtMaxDistanceBetweenSets(Collection<Point> set, ArrayList<Point> centers){
        int[] kj = ki.clone();
        for (Point p : set) {
            kj[p.getGroup()]--;
        }
        boolean complete = true;
        for (int kjj : kj) {
            if (kjj > 0) {
                complete = false;
                break;
            }
        }
        if (complete) {
            return false;
        }
        double maxD = 0;
        Point toInsert = null;
        for(Point p : set){
            double dd = p.getMaxDistance(centers);
            if (dd > maxD && kj[p.getGroup()] > 0) {
                maxD = dd;
                toInsert = p;
            }
        }
        if (toInsert != null) {
            centers.add(toInsert);
            return true;
        }
        return false;
    }

    private ArrayList<Point> partitionMatroidIntersectionSPARSE(TreeMap<Point, ArrayList<Point>> partition) {

        List<Triple<Integer, Integer, Double>> edges = new ArrayList<>(partition.size());
        //Node 0 is the source, node 1 to group.size() are the group nodes, nodes partition.size()+1 to
        int i = ki.length+1;
        for (Point pivot : partition.keySet()) {
            edges.add(new Triple<>(0, i, (double)1));
            int i_pivot = i;
            i++;
            for (Point p : partition.get(pivot)) {
                edges.add(new Triple<>(i_pivot, i, (double)1));
                int p_group = p.getGroup()+1;
                edges.add(new Triple<>(i, p_group, (double)1));
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

        //If the flow is less than the number of partitions
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
            for (Point p : partition.get(pivot)) {
                Integer edge = graph.getEdge(new_i, p.getGroup()+1);
                if (flows.get(edge) == 1) {
                    centers.add(p);
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
