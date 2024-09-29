import os.path
import numpy as np
from icecream import ic
import seaborn as sns
import polars as pl
import matplotlib.pyplot as plt

# Parameters to change
replace_commas = False
type_of_graph = "deltas"

# Parameters
x_axis = "wsize"
y_axis = ["update", "query", "memory", "ratio"]
color = "algorithm"

# File to read from
datasets = ["phones", "higgs", "covtype"]
file_names = []
for i in datasets:
    file_names.append(x_axis + "_" + i)
output_file = "graphs/" + type_of_graph + "_lines"

COLORS = sns.color_palette()
PALETTE = {
    "JONES": COLORS[0],
    "OursOblivious": COLORS[1],
    "Ours": COLORS[2],
    "CHEN": COLORS[3],
}

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
        pl.col("dataset") != "NORMALIZED",
    )
    df = df.with_columns(
        pl.col("update").str.replace(",", ".").cast(pl.Float64).alias("update"),
        pl.col("query").str.replace(",", ".").cast(pl.Float64).alias("query"),
        pl.col("radius").str.replace(",", ".").cast(pl.Float64).alias("radius"),
        pl.col("memory").str.replace(",", ".").cast(pl.Float64).alias("memory"),
        pl.col("ratio").str.replace(",", ".").cast(pl.Float64).alias("ratio"),
    )
    df = df.with_columns(
        pl.col("algorithm").str.extract(r"DELTA(\d+)").cast(pl.Float64).alias("delta") / 10
    )
    df = df.with_columns(
        pl.col("algorithm").str.replace(r"PELLCAPPDELTA(\d+)", "OursOblivious"),
    ).with_columns(
        pl.col("algorithm").str.replace(r"CAPPDELTA(\d+)", "Ours")
    ).filter(
        pl.col("algorithm").is_in(["JONES", "CHEN", "Ours", "OursOblivious"])
    )
    df = df.filter(pl.col("wsize").is_in([10000]))
    df = df.with_columns(
        ( pl.col("update") / 1e6 ).alias("update"),
        ( pl.col("query") / 1e6 ).alias("query")
    )
    return df


def hline(data, **kwargs):
    baseline = data[data["algorithm"].isin(["JONES", "CHEN"])]
    ax = plt.gca()
    for _, bline in baseline.iterrows():
        color_group = bline["algorithm"]
        yline = bline[kwargs["y"]]
        ax.axhline(yline, 
                   c=kwargs["palette"][color_group], 
                   linestyle=kwargs["dashes"][color_group])


def load(file, basedir="experiments_results/"):
    input_file = basedir + file + ".csv"
    if replace_commas:
        replace_dots_with_commas(input_file)
    df = pl.read_csv(source=input_file, separator=";", infer_schema_length=10000)
    df = ( df
        .with_columns(pl.lit(file.split("_")[-1].upper())
        .alias("dataset"))
    )
    if "wsize" not in df.columns:
        df = df.with_columns(pl.lit(10000, pl.Int64).alias("wsize"))
    if "type" in df.columns:
        df = df.filter(pl.col("type") == "Rand")
    df = df.select(
        "wsize", "algorithm", "update", "query", "radius", "ratio", "memory", "dataset"
    )
    # FILTERS
    df = filter(df)
    return df


def read_and_plot_bar(output_file_path):
    """
    Reads data from input files, performs filtering, and generates bar plots based on the provided parameters.

    Args:
        output_file_path (str): The path where the output plots will be saved.

    Returns:
        None
    """

    dataframe = []
    for dataset in datasets:
        # file = "wsize_jones_" + dataset
        # df = load(file)
        # dataframe.append(df)
        file = "type_jones_" + dataset
        df = load(file)#.filter(pl.col("algorithm") == "Ours")
        dataframe.append(df)
        # Add CHEN results
        file = "type_" + dataset
        df = load(file, "experiments_results/CHEN/").filter(pl.col("algorithm") == "CHEN")
        dataframe.append(df)
    dat = pl.concat(dataframe)
    dat = dat.filter(pl.col("wsize") == 10000)

    for graph in y_axis:
        g = sns.FacetGrid(
            dat,
            col="dataset",
            col_wrap=3,
            sharex=False,
            sharey= graph == "query",
            height=2,
            aspect=1.8,
        )
        g.map_dataframe(
            sns.lineplot,  # barplot or lineplot
            x="delta",  # x axis
            y=graph,  # y axis
            hue="algorithm",  # color
            palette=PALETTE
        )
        g.map_dataframe(
            sns.scatterplot,  # barplot or lineplot
            x="delta",  # x axis
            y=graph,  # y axis
            hue="algorithm",  # color
            size="algorithm",
            style="algorithm",
            palette=PALETTE,
            sizes=list( np.array([1, 1, 3]) * 50 ),
            size_order=["CHEN", "Ours", "OursOblivious"],
        )
        g.map_dataframe(
            hline,
            y=graph,  # y axis
            hue="algorithm",  # color
            palette=PALETTE,
            dashes={"CHEN": ":", "JONES": "--"}
        )
        g.set_xlabels(r"$\delta$", usetex=True)
        g.add_legend()
        if graph == "query":
            plt.yscale("log")
        plt.tight_layout(pad=0.0)
        plt.savefig(output_file_path + "_" + graph + ".png", dpi=300)  # save plot


# USE
read_and_plot_bar(output_file)
