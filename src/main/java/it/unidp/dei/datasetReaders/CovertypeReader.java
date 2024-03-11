package it.unidp.dei.datasetReaders;

import it.unidp.dei.Point;

import java.io.FileNotFoundException;
import java.util.Arrays;

public class CovertypeReader implements DatasetReader {
    private InputFileReader reader = null;
    public static final int dimension = 54;
    public void setFile(String fileName) throws FileNotFoundException {
        if (reader == null) {
            reader = new InputFileReader(fileName);
        }
    }
    @Override
    public boolean hasNext() {
        return reader.hasMoreTokens();
    }

    public Point nextPoint(int time, int wSize) {
        double[] coords = new double[dimension];
        for(int i=0; i<dimension; i++){
            coords[i] = reader.getDouble();
        }
        System.out.println(Arrays.toString(coords));

        return new Point(coords, time, wSize, reader.getInt()-1);
    }

    public void close() {
        reader.close();
    }

}
