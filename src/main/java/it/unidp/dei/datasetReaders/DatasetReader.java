package it.unidp.dei.datasetReaders;

import it.unidp.dei.Point;

import java.io.FileNotFoundException;

//Abstract class implemented by all the readers of a specific dataset
public abstract class DatasetReader {

    //Sets the data source
    public void setSource(String fileName) throws FileNotFoundException {
        reader = new InputFileReader(fileName);
    }

    //Returns next point of the input
    public abstract Point nextPoint(int time, int wSize);

    //Returns true only if there is a next point
    public boolean hasNext() {
        return reader.hasMoreTokens();
    }

    //Closes the file
    public void close() {
        if (reader != null) {
            reader.close();
        }
        reader = null;
    }

    protected InputFileReader reader = null;
}
