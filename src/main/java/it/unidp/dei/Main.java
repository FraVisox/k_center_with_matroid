package it.unidp.dei;

//Only contains the code to manage the commands passed by the user
public class Main {
    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("TESTS TO RUN:\n" +
                    "- cambia qualcosa tra originals e randomized: ro\n"+
                    "- cambiare il valore di wsize: w\n" +
                    "- cambiare il delta con varie dimensioni di window per vedere la variazione dei parametri: wd\n" +
                    "- aumento memoria/tempo in funzione di k: k\n" +
                    "- aumento memoria/tempo in funzione di epsilon: e\n" +
                    "- aumento memoria/tempo in funzione di beta: b\n"+
                    "- aumento memoria/tempo in funzione di doubling dimension: dd\n");
        } else if (args[0].equalsIgnoreCase("ro")) {
            System.out.println("\n----------------------\nSTART OF TEST OF RANDOMIZED DATASETS\n----------------------\n");
            TestUtils.testRandomized();
            System.out.println("\n----------------------\nTEST OF RANDOMIZED DATASETS FINISHED\n----------------------\n");
            System.out.println("\n----------------------\nSTART OF TEST OF ORIGINAL DATASETS\n----------------------\n");
            TestUtils.testOriginals();
            System.out.println("\n----------------------\nTEST OF ORIGINAL DATASETS FINISHED\n----------------------\n");
        } else if (args[0].equalsIgnoreCase("w")) {
            System.out.println("\n----------------------\nSTART OF TEST OF WSIZE\n----------------------\n");
            TestUtils.testWSize();
            System.out.println("\n----------------------\nWSIZE TEST FINISHED\n----------------------\n");
        } else if (args[0].equalsIgnoreCase("wd")) {
            System.out.println("\n----------------------\nSTART OF TEST OF DELTAS AND WSIZES\n----------------------\n");
            TestUtils.testDeltaW();
            System.out.println("\n----------------------\nDELTAS AND WSIZES TEST FINISHED\n----------------------\n");
        } else if (args[0].equalsIgnoreCase("b")) {
            System.out.println("\n----------------------\nSTART OF TEST OF BETA\n----------------------\n");
            TestUtils.testBeta();
            System.out.println("\n----------------------\nBETA TEST FINISHED\n----------------------\n");
        } else if (args[0].equalsIgnoreCase("k")) {
            System.out.println("\n----------------------\nSTART OF TEST OF KI\n----------------------\n");
            TestUtils.testKi();
            System.out.println("\n----------------------\nKI TEST FINISHED\n----------------------\n");
        } else if (args[0].equalsIgnoreCase("e")) {
            System.out.println("\n----------------------\nSTART OF TEST OF EPSILON\n----------------------\n");
            TestUtils.testEpsilon();
            System.out.println("\n----------------------\nEPSILON TEST FINISHED\n----------------------\n");
        } else if (args[0].equalsIgnoreCase("dd")) {
            System.out.println("\n----------------------\nSTART OF TEST OF BLOBS\n----------------------\n");
            BlobsTestUtils.testBlobs();
            System.out.println("\n----------------------\nBLOBS TEST FINISHED\n----------------------\n");
        }
    }
}
