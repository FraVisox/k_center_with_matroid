import seaborn as sns
import polars as pl
import matplotlib.pyplot as plt

#Parameters to change
replace_commas = False
type_of_graph = "wsize" #beta, wsize, type, k

#File to read from
datasets = ["phones", "higgs", "covtype", "normalized", "random"]
file_names = []
for i in datasets:
    if (type_of_graph == "type" and (i == "random" or i == "higgs")):
            continue
    file_names.append(type_of_graph+"_"+i)
output_file = "graphs/"+type_of_graph

#Parameters
x_axis = type_of_graph
if (type_of_graph == "deltas"):
    x_axis = "wsize"
y_axis = ["update", "query", "radius", "ratio", "memory"]
color = 'algorithm'

pal = dict(
    CHEN="#f22020", #red 
    CAPP="#96341c", #brown
    COHCAPP="#8E8E38", #gold
    PELLCAPP="#f47a22", #orange
    CAPPDELTA05="#7dfc00", #light green
    COHCAPPDELTA05="#008B8B", #verde acqua
    PELLCAPPDELTA05="#0ec434", #green
    CAPPVAL="#8B8B83", #grey
    COHCAPPVAL="#b732cc", #purple
    PELLCAPPVAL="#2f2aa0", #dark blue
    PELLCAPPDELTA10="#1E90FF", #blue
    PELLCAPPDELTA15="#EE00EE", #fucsia
    PELLCAPPDELTA20="#772b9d", #dark purple
    PELLCAPPDELTA25="#228c68", #dark green
    PELLCAPPDELTA30="#00E5EE", #light blue
    PELLCAPPDELTA35="#f07cab", #pink
    PELLCAPPDELTA40="#000000", #black
    )


def replace_dots_with_commas(file_path):
    """
    Replaces all commas in the file at the specified file path with dots.
    
    Args:
        file_path (str): The path to the file to be modified.
    
    Returns:
        None
    
    Raises:
        FileNotFoundError: If the file at the specified file path is not found.
        Exception: If an error occurs during the replacement process.
    """
    try:
        # Read the content of the file
        with open(file_path, 'r') as file:
            content = file.read()

        # Replace all commas with dots
        modified_content = content.replace(',', '.')

        # Write the modified content back to the file
        with open(file_path, 'w') as file:
            file.write(modified_content)

        print("Dots have been replaced with commas successfully.")

    except FileNotFoundError:
        print(f"The file at {file_path} was not found.")
    except Exception as e:
        print(f"An error occurred: {e}")



def filter(df):
    """
    Filters the given DataFrame `df` depending on the type of graph.

    Args:
        df (pl.DataFrame): The DataFrame to be filtered.

    Returns:
        pl.DataFrame: The filtered DataFrame.
    """
    if (type_of_graph == "beta"):
        df = df.filter(pl.col("beta").is_between(0,50))
    elif (type_of_graph == "wsize"):
        df = df.filter(pl.col("wsize").is_between(0,100000))
        df = df.filter(pl.col("algorithm").is_in(["CHEN", "PELLCAPP", "PELLCAPPDELTA05", "PELLCAPPDELTA10", "PELLCAPPDELTA15", "PELLCAPPDELTA20"]))
    elif (type_of_graph == "type"):
        df = df.filter(pl.col("algorithm").is_in(["CHEN", "PELLCAPP", "PELLCAPPDELTA05", "PELLCAPPDELTA10", "PELLCAPPDELTA15", "PELLCAPPDELTA20", "PELLCAPPDELTA25", "PELLCAPPDELTA30", "PELLCAPPDELTA35", "PELLCAPPDELTA40"]))
    return df

def read_and_plot_line(output_file_path):
    """
    Reads data from input files, performs filtering, and generates line plots based on the provided parameters.
    
    Args:
        output_file_path (str): The path where the output plots will be saved.

    Returns:
        None
    """
    dataframe = [] 
    for file in file_names:
        input_file = "experiments_results/"+file+".csv"
        if replace_commas:
            replace_dots_with_commas(input_file)
        df = pl.read_csv(source=input_file, separator=";")
        df = df.with_columns(
            pl.lit(file.split("_")[1].upper()).alias("dataset")
        )
        df = df.with_columns(
            pl.col("query").cast(pl.Float64).alias("query")
        )
        #FILTERS
        df = filter(df)
        dataframe.append(df)
    dat = pl.concat(dataframe)

    for graph in y_axis:
        g = sns.FacetGrid(dat, col="dataset", col_wrap=3, sharex=False, sharey=False, aspect=2)
        g.map_dataframe(
            sns.lineplot,  #barplot or lineplot
            x    = x_axis,   #x axis
            y    = graph, #y axis
            hue  = color, #color
            marker="o",
            palette=pal,
            linewidth=3,
            hue_order = ["PELLCAPPDELTA20", "PELLCAPPDELTA15", "PELLCAPPDELTA10", "PELLCAPPDELTA05",  "PELLCAPP", "CHEN"]
            )
        g.add_legend()
        plt.savefig(output_file_path+"_"+graph+".png") #save plot

def read_and_plot_bar_type(output_file_path):
    """
    Reads data from input files, performs filtering, and generates bar plots based on the provided parameters.
    
    Args:
        output_file_path (str): The path where the output plots will be saved.

    Returns:
        None
    """
    dataframe = [] 
    for file in file_names:
        input_file = "experiments_results/"+file+".csv"
        if replace_commas:
            replace_dots_with_commas(input_file)
        df = pl.read_csv(source=input_file, separator=";", infer_schema_length=10000)
        df = df.with_columns(
            pl.lit(file.split("_")[1].upper()).alias("dataset")
        )
        #FILTERS
        df = filter(df)
        dataframe.append(df)
    dat = pl.concat(dataframe)

    for graph in y_axis:
        g = sns.FacetGrid(dat, col="dataset", col_wrap=3, sharex=False, sharey=False, aspect=2)
        g.map_dataframe(
            sns.barplot,  #barplot or lineplot
            x    = x_axis,   #x axis
            y    = graph, #y axis
            hue  = color, #color
            palette=pal
            )
        g.add_legend()
        plt.savefig(output_file_path+"_"+graph+".png") #save plot

# USE
if (type_of_graph == "type"):
    read_and_plot_bar_type(output_file)
else:
    read_and_plot_line(output_file)