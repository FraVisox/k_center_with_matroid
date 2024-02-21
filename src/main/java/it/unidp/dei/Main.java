package it.unidp.dei;

import it.unidp.dei.datasetReaders.DatasetReader;
import it.unidp.dei.datasetReaders.PhonesReader;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class Main {
    private static final String[] datasets = {"src/main/java/it/unidp/dei/data/Phones/Phones_accelerometer.csv"};

    //TODO: il numero di centri puo' essere minore di k? Per come è ora l'algoritmo si. Volendo si potrebbe aumentare in modo da ottenere k aggiungendo punti a caso della categoria giusta
    private static final int[] ki = {5, 5, 5, 5, 5, 5, 5};
    private static final double epsilon = 1;
    private static final double beta = 1;

    //PHONES: maxD = 30 and minD = 5e-4 (tested for 840000 points)
    //HIGGS: maxD = 29 and minD = 0.008 (tested for 620000 points). Pellizzoni used 100 and 0.01
    private static final double minDist = 5e-4;
    private static final double maxDist = 30;
    private static final int wSize = 1000;
    public static final double INF = maxDist+1;

    //It tells how many times we will query the algorithms after having a complete window
    private static final int stride = 1;

    public static void main(String[] args) {
        DatasetReader reader;
        PrintWriter writerChen;
        PrintWriter writerCapp;
        for (String set : datasets) {

            try {
                //Create a dataset reader
                reader = new PhonesReader(set);
                //Create a results writer
                writerChen = new PrintWriter("out/testPhonesCHEN.csv");
                writerCapp = new PrintWriter("out/testPhonesCAPP.csv");

            } catch (FileNotFoundException e) {
                System.out.println("File " + set + " not found, skipping to next dataset");
                continue;
            }

            //TEST THINGS
            testAlgorithms(reader, writerChen, writerCapp);


            //FLUSH AND CLOSE
            writerChen.flush();
            writerCapp.flush();

            writerChen.close();
            writerCapp.close();

            reader.close();
        }
    }

    //In every line of the output file we will have:
    //updateTime;queryTime;radius;independence;memory
    private static void testAlgorithms(DatasetReader reader, PrintWriter writerChen, PrintWriter writerCapp) {
        long startTime, endTime;
        System.out.println("In every line of the output file we will have:");
        System.out.println("updateTime;queryTime;radius;independence;memory\n\n");

        //Testing LinkedList, contains all the window
        LinkedList<Point> window = new LinkedList<>();

        //Initialize the algorithm
        CAPP capp = new CAPP(ki, epsilon, beta, minDist, maxDist);
        CHEN chen = new CHEN(ki);

        for (int time = 0; time <= wSize+stride; time++) {

            //Check of passing of time, only used for debug
            if (time % wSize == 0 && time != 0) {
                System.out.println(time);
            }

            Point p = reader.nextPoint(time, wSize);

            if (p == null) {
                continue;
            }

            //If window is not full, we don't query
            if (time < wSize) {
                window.addLast(p);
                capp.update(p, time);
                chen.update(p, time);
                continue;
            }

            //Update the window
            window.addLast(p);
            window.removeFirst();

            //1. TIME TEST: we call explicitly the garbage collector to allow our algorithm
            //              to run without having to wait for the garbage collector

            //1a. TIME TEST OF UPDATE
            System.gc();
            startTime = System.nanoTime();
            chen.update(p, time);
            endTime = System.nanoTime();
            System.out.println("TEMPO UPDATE CHEN: "+(endTime-startTime)+"\n");
            //Write on file the time of update
            writerChen.print((endTime-startTime)+";");

            System.gc();
            startTime = System.nanoTime();
            capp.update(p, time);
            endTime = System.nanoTime();
            System.out.println("TEMPO UPDATE CAPP: "+(endTime-startTime)+"\n");
            //Write on file the time of update
            writerCapp.print((endTime-startTime)+";");


            //1b. TIME TEST OF QUERY
            ArrayList<Point> centersChen;
            ArrayList<Point> centersCapp;

            System.out.println("CHEN:");
            System.gc();
            startTime = System.nanoTime();
            centersChen = chen.query();
            endTime = System.nanoTime();
            System.out.println("TEMPO QUERY CHEN: "+(endTime-startTime)+"\n");
            //Write on file the time of query
            writerChen.print((endTime-startTime)+";");


            System.out.println("CAPP:");
            System.gc();
            startTime = System.nanoTime();
            centersCapp = capp.query();
            endTime = System.nanoTime();
            if (centersCapp.isEmpty()) {
                System.out.println("Max or min distances are not correct. There isn't a valid guess");
                return;
            }
            System.out.println("TEMPO QUERY CAPP: "+(endTime-startTime)+"\n");
            //Write on file the time of query
            writerCapp.print((endTime-startTime)+";");


            //2. QUALITY TEST: Check of the radius of the centers returned and the independence of the set
            double radius = maxDistanceBetweenSets(window, centersChen);
            System.out.println("RAGGIO CHEN: "+radius);
            boolean independence = isIndependent(centersChen);
            System.out.println("INDIPENDENZA CHEN: "+(independence ? "Verificata" : "NON SUSSISTE"));
            //Write on file
            writerChen.print(radius+";"+(independence ? "t": "f")+";");

            radius = maxDistanceBetweenSets(window, centersCapp);
            System.out.println("RAGGIO CAPP: "+radius);
            independence = isIndependent(centersCapp);
            System.out.println("INDIPENDENZA CAPP: "+(independence ? "Verificata" : "NON SUSSISTE"));
            writerCapp.print(radius+";"+(independence ? "t": "f")+";");


            //3. MEMORY TEST: we only sum the number of points for every algorithm
            int memory = chen.getSize();
            System.out.println("MEMORIA CHEN: "+memory);
            //Write on file
            writerChen.println(memory);

            memory = capp.getSize();
            System.out.println("MEMORIA CAPP: "+memory);
            //Write on file
            writerCapp.println(memory);
        }
    }

    private static double maxDistanceBetweenSets(Collection<Point> set, Collection<Point> centers){
        double ans = 0;
        for(Point p : set){
            double dd = INF+1;
            for(Point q : centers){
                dd = Math.min(dd, p.getDistance(q));
            }
            ans = Math.max(dd, ans);
        }
        return ans;
    }

    private static boolean isIndependent(Collection<Point> set) {
        int[] kj = ki.clone();
        for (Point p : set) {
            kj[p.getGroup()] -= 1;
            if (kj[p.getGroup()] < 0) {
                return false;
            }
        }
        return true;
    }

    private static void calculateMinMaxDist(DatasetReader reader) {
        double maxD = 0, minD = INF;
        ArrayList<Point> window = new ArrayList<>(wSize);

        for (int time = 0; reader.hasNext(); time++) {
            Point p = reader.nextPoint(time, wSize);
            window.add(p);

            maxD = Math.max(maxD, p.getMaxDistance(window));
            minD = Math.min(minD, p.getMinDistance(window));

            if (time % 10000 == 0) {
                System.out.println("Al tempo: " + time);
                System.out.println("Minima distanza: " + minD);
                System.out.println("Massima distanza: " + maxD+"\n");
            }
        }
        System.out.println("Distanze finali:");
        System.out.println("Minima distanza: "+minD);
        System.out.println("Massima distanza: "+maxD);
    }
}
