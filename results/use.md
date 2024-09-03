# Use of this folder
The input folder is /experiments_results/ (csv files created from the results of the experiments).
The output folder is /graphs/, where all the png images will be put.

## Use of files
- delta_bars.py will make bars of different CAPPDELTAxx runs on different window sizes;
- doubling_dim.py will interpret the data in /experiment_results/doubling_dimension.csv and create lines to understand what changes when doubling dimension changes;
- lines.py can be used to make lines graphs of wsize, beta, k and differences between random and original;
- phase_1.py will interpret data in type and make comparisons between different versions of the same algorithm (CAPP, COHCAPP and PELLCAPP for example).


## Documentation:
- POLARS
https://docs.pola.rs/api/python/stable/reference/api/polars.read_csv.html
- SEABORN
https://seaborn.pydata.org/tutorial/introduction.html