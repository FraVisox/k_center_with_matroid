package it.unidp.dei;

import org.jgrapht.alg.flow.PushRelabelMFImpl;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.util.*;

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
        pts.add(p);
    }

    public ArrayList<Point> query() {
        double[] distances = new double[pts.size()* pts.size()- pts.size()];
        int i = 0;
        for (Point p : pts) {
            for (Point q : pts) {
                //By doing this, we delete all the zero distances
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
                    break;
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
            graph.addVertex("pivot"+pivot.toString());

            //Connect every pivot with the source, capacity 1
            DefaultWeightedEdge e = graph.addEdge("s", "pivot"+pivot.toString());
            graph.setEdgeWeight(e, 1);

            for (Point p : partition.get(pivot)) {

                //Add a vertex m+exitTime for every point
                graph.addVertex("point"+p.toString());
                //Add a vertex r+indexOfGroup for every group
                graph.addVertex("group"+Integer.toString(p.getGroup()));

                //Connect every head of partition with its points, capacity 1
                e = graph.addEdge("pivot"+pivot.toString(), "point"+p.toString());
                graph.setEdgeWeight(e, 1);

                //Connect every point with its group, capacity 1
                e = graph.addEdge("point"+p.toString(), "group"+Integer.toString(p.getGroup()));
                //If the edge does not exist, it sets its capacity
                if (e != null) {
                    graph.setEdgeWeight(e, 1);
                }
            }
        }

        //Add sink vertex
        graph.addVertex("t");

        for (int i = 0; i < ki.length; i++) {
            //Add all the remaining vertices for the other groups
            graph.addVertex("group"+Integer.toString(i));

            //Connect the groups with the sink, capacity ki of that group
            DefaultWeightedEdge e = graph.addEdge("group"+i, "t");
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

        ArrayList<Point> centers = new ArrayList<>((int)flow);

        //For every point of the partition if the flow value of the edge that connects it to its group is 1, we add it to the centers
        //The points of the partition are not all the points that are in pts
        int i = 0;
        for (Point pivot : partition.keySet()) {
            for (Point p : partition.get(pivot)) {
                DefaultWeightedEdge edge = graph.getEdge("point" + p, "group" + p.getGroup());
                if (flows.get(edge) == 1) {
                    centers.add(p);
                }
            }
        }
        return centers;
    }

    private final LinkedList<Point> pts;
    private final int[] ki;
    private final int k;
}
