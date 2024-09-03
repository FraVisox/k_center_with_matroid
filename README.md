# K-CENTER CLUSTERING WITH PARTITION MATROID (FAIR K-CENTER CLUSTERING)
The following project is the bachelor thesis of Francesco Visona'.

## FAIR K-CENTER CLUSTERING
The problem addressed is the Fair K-Center Clustering, a problem of K-Center Clustering where the set of centers calculated by the algorithm should 
be an independent set for a given partition matroid. This problem is addressed on the sliding window model.
The theory behind this project, and the results of the successive analysis can be found
in the file Thesis.pdf, and rely on the work of Cappellotto e al. (references in the thesis).

## Execution
To execute the program:
1. Install the maven project: the output will be a file FKC_sliding_window-1.0-jar-with-dependencies.jar in the /target/ folder;
2. Make sure to put the jar file in the same directory of a /data/ directory and an /out/ directory: the first
   one will be the one the datasets are (randomized and original, as explained in the section Data), and the other one
   will be where the output files will be put;
3. Execute 
```
java -jar FKC_sliding_window-1.0-jar-with-dependencies.jar 
```
with one of the following options
   - ro to tests differences between originals and randomized (and also oblivious and not)
   - kalg to tests K-Algorithms
   - b to tests algorithms when beta changes
   - w to tests algorithms when the window size changes
   - dd to tests algorithms when doubling dimension changes
   - k to tests algorithms when K changes

## Data
In order to use the original datasets it is necessary to download them from their sources and put them in /data/originals/:
- HIGGS: https://archive.ics.uci.edu/dataset/280/higgs
- PHONES: https://archive.ics.uci.edu/dataset/344/heterogeneity+activity+recognition
- COVERTYPE: https://archive.ics.uci.edu/dataset/31/covertype

To create NORMALIZED, it is sufficient to run NormalizeDataset. To create all the other randomized datasets, you could run RandomizeDataset.
All the files used for our tests can be found here:
https://drive.google.com/drive/folders/1YraBr_UZhe9sNAXeCGSiTX9hPQdQDIWW?usp=drive_link

These can be downloaded and put in folders to have the following structure:
- /data/originals will contain all the datasets as they can be found online;
- /data/randomized will contain all the datasets generated (RANDOM and BLOBS) and the other datasets, except for
  HIGGS, but with lines swapped randomly.

## Results
Results are produced in the /out/ folder, that needs to be in the same directory as the jar file.
We analyzed the results of some tests, calculating the arithmetic mean and putting them as tidy data in the /results/ folder.
In that folder can be found:
- /results/distances/ contains the distances calculated with the class CalculateMinMaxDist for all the datasets, both originals and randomized
- /results/distributions_of_ki/ contains all the distributions of k_i calculated with the class FindNumberOfK for all the datasets, both originals and randomized
- /results/experiments_results/ contains all the results of the experiments as tidy data, as explained in /results/use.md
- /results/graphs/ contains the graphs created from the tidy data
- all the algorithms (*.py) used to create the graphs, which use is explained in /results/use.md