package it.unidp.dei.datasetReaders;

import it.unidp.dei.Point;

import java.io.FileNotFoundException;

public abstract class DatasetReader {
    public void setFile(String fileName) throws FileNotFoundException {
        reader = new InputFileReader(fileName);
    }
    public abstract Point nextPoint(int time, int wSize);
    public boolean hasNext() {
        return reader.hasMoreTokens();
    }
    public void close() {
        if (reader != null) {
            reader.close();
        }
        reader = null;
    }
    protected InputFileReader reader = null;
}
