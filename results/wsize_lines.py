import numpy as np
from icecream import ic
import seaborn as sns
import polars as pl
import matplotlib.pyplot as plt

# Parameters to change
replace_commas = False
type_of_graph = "wsize"

# Parameters
x_axis = "wsize"
y_axis = ["update", "query", "memory"] # we can omit the ratio because it is rather uninteresting: we always get the same result as Chen
color = "algorithm"

# File to read from
datasets = ["phones", "higgs", "covtype"]
file_names = []
for i in datasets:
    file_names.append(x_axis + "_" + i)
output_file = "graphs/" + type_of_graph + "_lines"


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
        with open(file_path, "r") as file:
            content = file.read()

        # Replace all commas with dots
        modified_content = content.replace(",", ".")

        # Write the modified content back to the file
        with open(file_path, "w") as file:
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
    df = df.filter(
        pl.col("dataset") != "RANDOM",
        pl.col("algorithm") != "PELLCAPP",
        pl.col("dataset") != "NORMALIZED",
    )
    df = df.with_columns(
        pl.col("algorithm").str.extract(r"DELTA(\d+)").cast(pl.Float64).alias("delta") / 10
    )
    df = df.with_columns(
        pl.col("algorithm").str.replace(r"PELLCAPPDELTA(\d+)", "OursOblivious"),
    ).with_columns(
        pl.col("algorithm").str.replace(r"CAPPDELTA(\d+)", "Ours")
    ).filter(
        pl.col("algorithm").is_in(["CHEN", "Ours", "OursOblivious"])
    )
    # df = df.filter(pl.col("wsize").is_in([10000, 50000, 500000]))
    df = df.with_columns(
        ( pl.col("update") / 1e6 ).alias("update"),
        ( pl.col("query") / 1e6 ).alias("query")
    ).filter(pl.col("wsize") >= 10000)
    return df


def hline(data, **kwargs):
    baseline = data[data["algorithm"] == "CHEN"]
    ax = plt.gca()
    for bline in baseline[kwargs["y"]]:
        ax.axhline(bline, c=kwargs["color"])


def read_and_plot_bar(output_file_path):
    """
    Reads data from input files, performs filtering, and generates bar plots based on the provided parameters.

    Args:
        output_file_path (str): The path where the output plots will be saved.

    Returns:
        None
    """

    dataframe = []
    for file in file_names:
        if file == "random" or file == "higgs":
            continue
        input_file = "experiments_results/" + file + ".csv"
        if replace_commas:
            replace_dots_with_commas(input_file)
        df = pl.read_csv(source=input_file, separator=";", infer_schema_length=10000)
        df = df.with_columns(pl.lit(file.split("_")[1].upper()).alias("dataset"))
        # FILTERS
        df = filter(df)
        dataframe.append(df)
    dat = ( pl
        .concat(dataframe)
        .filter(( pl.col("delta") == 0.5 ) | (pl.col("delta").is_null()))
    )

    ic(dat)

    for graph in y_axis:
        g = sns.FacetGrid(
            dat,
            col="dataset",
            col_wrap=3,
            sharex=False,
            sharey= True,
            height=3,
            aspect=1.2,
        )
        g.map_dataframe(
            sns.lineplot,  # barplot or lineplot
            x="wsize",  # x axis
            y=graph,  # y axis
            hue="algorithm",  # color
            # style="algorithm",
            # markers=True,
            # dashes=False,
        )
        g.map_dataframe(
            sns.scatterplot,  # barplot or lineplot
            x="wsize",  # x axis
            y=graph,  # y axis
            hue="algorithm",  # color
            size="algorithm",
            style="algorithm",
            sizes=list( np.array([1, 1, 2]) * 50 ),
            size_order=["CHEN", "Ours", "OursOblivious"],
        )
        g.set_xlabels("window size")
        g.add_legend()
        if graph == "query":
            plt.yscale("log")
        plt.tight_layout()
        plt.savefig(ic(output_file_path + "_" + graph + ".png"), dpi=300)  # save plot


# USE
read_and_plot_bar(output_file)
