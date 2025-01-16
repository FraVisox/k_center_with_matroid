package it.unidp.dei.datasetReaders;

import it.unidp.dei.Point;

//Reader of PHONES
public class RotatedPhonesReader extends DatasetReader {

    public void setDimension(int dim) {
        dimension = dim;
    }

    @Override
    public Point nextPoint(int time, int wSize) {

        //Coordinates to be saved (x,y,z)
        double[] coords = new double[dimension];
        for(int i=0; i<dimension; i++) {
            coords[i] = reader.getDouble();
        }

        //Category:
        int category = getCategory(reader.getWord());
        if (category == -1) {
            return null;
        }

        return new Point(coords, time, wSize, category);
    }

    //The activities are: bike, sit, stand, walk, stairsup, stairsdown and null
    private static int getCategory(String s) {
        switch (s) {
            case "bike": return 0;
            case "sit": return 1;
            case "stand": return 2;
            case "walk": return 3;
            case "stairsup": return 4;
            case "stairsdown": return 5;
            case "null": return 6;
            default: return -1;
        }
    }
    private static int dimension = 3;
}
