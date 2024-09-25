package it.unidp.dei.JONES;

import it.unidp.dei.Algorithm;
import it.unidp.dei.Point;
import it.unidp.dei.TestUtils;
import org.jgrapht.alg.flow.*;
import org.jgrapht.alg.interfaces.MaximumFlowAlgorithm.*;

import org.jgrapht.graph.*;


import java.util.*;

//Implementation of the state of the art Jones et al. algorithm
public class JONES implements Algorithm {

    //Approximation of sequential algorithm. In our case, JONES gives a 3-approximation
    public static final int alfa = 3;

    public JONES(int[] _ki) {
        pts = new LinkedList<>();
        ki = _ki;
        k = Algorithm.calcK(_ki);
    }

    public JONES(LinkedList<Point> p, int[] _ki) {
        pts = p;
        ki = _ki;
        k = Algorithm.calcK(_ki);
    }

    //Updating only means to add the point to the list of points, and maybe delete another one
    @Override
    public void update(Point p, int time) {
        if (!pts.isEmpty() && pts.getFirst().hasExpired(time)) {
            pts.removeFirst();
        }
        pts.addLast(p);
    }

    //The implementation is the one described in Appendix 2 of the Jones et al. paper
    @Override
    public ArrayList<Point> query() {

        // ALGORITHM 1, as Gonzalez
        ArrayList<Pair> C = new ArrayList<>();
        C.add(new Pair(TestUtils.INF, pts.get(0)));

        //For each point, save its distance to the centers (to take the farthest one)
        ArrayList<Pair> A = new ArrayList<>();
        for (int j = 1; j < pts.size(); j++) {
            A.add(new Pair(pts.get(j).getDistance(pts.get(0)), pts.get(j)));
        }

        //Add as centers the farthest from the ones already taken
        for (int i = 2; i <= k; i++) {
            Point point_to_add = null;
            double max = 0;

            for (Pair pp : A) {
                if (pp.getD() > max) {
                    point_to_add = pp.getP();
                    max = pp.getD();
                }
            }

            //In C we also put the minimum distance from previous centers
            C.add(new Pair(max, point_to_add));

            //Update distances
            for (Pair pp : A) {
                double dist = pp.getP().getDistance(point_to_add);
                if (dist < pp.getD()) {
                    pp.setD(dist);
                }
            }
        }

        //Now we have in C all the centers and the minimum distance from the previous points.

        //PHASE 2: Search the maximum h such that the first h centers satisfy the fair shift constraint
        int h_lo = 0;
        int h_hi = k;

        //Create a base graph graph0 with only source, sink and groups
        DefaultDirectedWeightedGraph<String, RelationshipEdge> graph0 = new DefaultDirectedWeightedGraph<>(RelationshipEdge.class);

        graph0.addVertex("source");
        graph0.addVertex("sink");

        for (int i = 0; i < ki.length; i++) {
            RelationshipEdge connection = new RelationshipEdge();
            graph0.addVertex("group"+i);
            graph0.addEdge("group"+i, "sink", connection);
            graph0.setEdgeWeight(connection, ki[i]);
        }

        //Clone that graph in graph1 (that will be used in this phase)
        DefaultDirectedWeightedGraph<String, RelationshipEdge> graph1 = (DefaultDirectedWeightedGraph<String, RelationshipEdge>) graph0.clone();

        //Binary search on distances saved in C
        while (h_lo != h_hi) {

            DefaultDirectedWeightedGraph<String, RelationshipEdge> graph = (DefaultDirectedWeightedGraph<String, RelationshipEdge>) graph1.clone();

            int l = (int) Math.ceil(((double) (h_hi + h_lo)) /2);

            for (int j = h_lo; j < l; j++) {
                RelationshipEdge connection = new RelationshipEdge();
                graph.addVertex("v" + C.get(j).getP().toString());
                graph.addEdge("source", "v" + C.get(j).getP().toString(), connection);
                graph.setEdgeWeight(connection, 1);

                //Calculate the closest point to aj for each demographic group f by a single sweep of S
                double[] min_dists = new double[ki.length];

                for (int i = 0; i < ki.length; i++) {
                    min_dists[i] = TestUtils.INF;
                }

                for (Point p : pts) {
                    if (p.getDistance(C.get(j).getP()) < min_dists[p.getGroup()]) {
                        min_dists[p.getGroup()] = p.getDistance(C.get(j).getP());
                    }
                }

                //If that distance is less than the one in C divided by 2, add an edge
                for (int i = 0; i < ki.length; i++) {
                    if (min_dists[i] <= C.get(l - 1).getD() / 2) {
                        RelationshipEdge connection1 = new RelationshipEdge();
                        graph.addEdge("v" + C.get(j).getP().toString(), "group" + i, connection1);
                        graph.setEdgeWeight(connection1, 1);
                    }
                }
            }

            //Use the Dinic algorithm to calculate the flow from source to sink
            DinicMFImpl<String, RelationshipEdge> alg = new DinicMFImpl<>(graph);
            MaximumFlow<RelationshipEdge> flow = alg.getMaximumFlow("source", "sink");

            //If this flow is l, this is a possible h
            if (flow.getValue() == l) {
                //We save the graph and don't discard it
                graph1 = (DefaultDirectedWeightedGraph<String, RelationshipEdge>) graph.clone();
                h_lo = l;
            } else {
                h_hi = l-1;
            }

        }

        //Now we have that h == h_lo == h_hi

        //graph1 = (DefaultDirectedWeightedGraph<String, RelationshipEdge>) graph0.clone(); NO NEED TO CLONE

        //Add all the vertices for the first h centers
        for (int j = 0; j < h_lo; j++) {
            graph0.addVertex("v"+C.get(j).getP().toString());
            RelationshipEdge connection1 = new RelationshipEdge();
            graph0.addEdge("source", "v"+C.get(j).getP().toString(), connection1);
            graph0.setEdgeWeight(connection1, 1);
        }

        //Find the minimum distances between the centers and the closest points for every category
        ArrayList<Pair> R = new ArrayList<>();

        for (int j = 0; j < h_lo; j++) {
            double[] min_dists = new double[ki.length];
            Point[] min_pts = new Point[ki.length];

            for (int i = 0; i<ki.length;i++) {
                min_dists[i] = TestUtils.INF;
                min_pts[i] = null;
            }

            for (Point p : pts) {
                if (p.getDistance(C.get(j).getP()) < min_dists[p.getGroup()]) {
                    min_dists[p.getGroup()] = p.getDistance(C.get(j).getP());
                    min_pts[p.getGroup()] = p;
                }
            }

            for (int i = 0; i<ki.length;i++) {
                R.add(new Pair(min_dists[i], min_pts[i], C.get(j).getP()));
            }
        }

        //We sort it as we will perform a binary search
        Collections.sort(R);

        Map<RelationshipEdge, Double> F_first = null; //So that we will have null pointer exception if something goes wrong
        while (!R.isEmpty()) {
            DefaultDirectedWeightedGraph<String, RelationshipEdge> graph = (DefaultDirectedWeightedGraph<String, RelationshipEdge>) graph0.clone();

            //Take the median
            double median;
            if (R.size() % 2 == 0) {
                median = (R.get((R.size()-1)/2).getD() + R.get((R.size()-1)/2).getD()) / 2;
            } else {
                median = R.get(R.size()/2).getD();
            }

            //Save which elements of R we should remove after and make edges for vertices with distances less than the median
            int remove_first = 0;
            int remove_last = 0;
            for (int i = 0; i<R.size(); i++) {
                Pair pair = R.get(i);
                if (pair.getD() > median) {
                    if (remove_last == 0) {
                        remove_last = R.size()-i;
                    }
                    break;
                }
                if (pair.getD() == median) {
                    remove_last = R.size()-i;
                }
                RelationshipEdge connection1 = new RelationshipEdge(pair.getP());
                graph.addEdge("v"+pair.getVj().toString(), "group"+pair.getP().getGroup(), connection1);
                graph.setEdgeWeight(connection1, 1);
                remove_first++;
            }

            //Get max flow with Dinic algorithm
            DinicMFImpl<String, RelationshipEdge> alg = new DinicMFImpl<>(graph);
            MaximumFlow<RelationshipEdge> flow = alg.getMaximumFlow("source", "sink");

            if (flow.getValue() == h_lo) {
                F_first = flow.getFlowMap();
                for (int i = 0; i< remove_last; i++) {
                    R.remove(R.size()-1);
                }
            } else {
                graph0 = (DefaultDirectedWeightedGraph<String, RelationshipEdge>) graph.clone();
                if (remove_first > 0) {
                    R.subList(0, remove_first).clear();
                }
            }

        }

        //First, the centers are taken from the labels of the flow in the last step
        ArrayList<Point> centers = new ArrayList<>();

        for (RelationshipEdge edge : F_first.keySet()) {
            if (F_first.get(edge) == 1 && edge.getLabelPoint() != null) {
                centers.add(edge.getLabelPoint());
            }
        }


        //Then we add the farthest points to the centers
        while (centers.size() < k && !insertPointAtMaxDistanceBetweenSets(pts, centers)) {
            //The loop updates the centers, but this is done in the condition
        }

        return centers;
    }

    //Utility function that fills centers with one point, the farthest from the centers of the right category
    private boolean insertPointAtMaxDistanceBetweenSets(Collection<Point> set, ArrayList<Point> centers){
        int[] kj = ki.clone();
        for (Point p : centers) {
            kj[p.getGroup()]--;
        }

        //Take the point at the maximum distance from the centers
        double maxD = 0;
        Point toInsert = null;
        for(Point p : set){
            double dd = p.getMinDistance(centers);
            if (dd > maxD && kj[p.getGroup()] > 0) {
                maxD = dd;
                toInsert = p;
            }
        }

        if (toInsert != null) {
            centers.add(toInsert);
        }

        return toInsert == null;
    }

    @Override
    public int getSize() {
        return pts.size();
    }

    private final LinkedList<Point> pts;
    private final int[] ki;
    private final int k;
}
