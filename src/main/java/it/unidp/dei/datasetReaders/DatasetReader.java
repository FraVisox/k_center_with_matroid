package it.unidp.dei.datasetReaders;

import it.unidp.dei.Point;

public interface DatasetReader {
    public Point nextPoint(int time, int wSize);
    public boolean hasNext();
    public void close();
}
