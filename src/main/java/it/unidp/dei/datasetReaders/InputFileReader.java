package it.unidp.dei.datasetReaders;

import java.io.*;
import java.util.StringTokenizer;

//This entire class is similar to the one in Pellizzoni e al. code, for reference see the paper.
//It is used to read a generic dataset
public class InputFileReader {
    public InputFileReader(String fileName) throws FileNotFoundException {
        reader = new BufferedReader(new java.io.FileReader(fileName));
    }

    public boolean hasMoreTokens() {
        return peekToken() != null;
    }

    public int getInt() {
        return Integer.parseInt(nextToken());
    }

    public double getDouble() {
        return Double.parseDouble(nextToken());
    }

    public String getWord() {
        return nextToken();
    }

    public void close() {
        try {
            reader.close();
        } catch (IOException ignored) {}
    }

    public String getLine(){
        try{
            tokenizer = null;
            token = null;
            return reader.readLine();
        }
        catch(IOException ignored){}
        return null;
    }

    public void skipFirstLine(){
        tokenizer = null;
        token = null;
    }

    private String peekToken() {
        if (token == null)
            try {
                while (tokenizer == null || !tokenizer.hasMoreTokens()) {
                    String line = reader.readLine();
                    if (line == null) return null;
                    tokenizer = new StringTokenizer(line, " \t\n\r\f,;");
                }
                token = tokenizer.nextToken();
            } catch (IOException ignored) { }
        return token;
    }

    private String nextToken() {
        String ans = peekToken();
        token = null;
        return ans;
    }

    private final BufferedReader reader;
    private StringTokenizer tokenizer;
    private String token;
}