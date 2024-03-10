package it.unidp.dei.datasetReaders;

import it.unidp.dei.Point;

import java.io.FileNotFoundException;

public interface DatasetReader {
    public void setFile(String fileName) throws FileNotFoundException;
    public Point nextPoint(int time, int wSize);
    public boolean hasNext();
    public void close();
}
