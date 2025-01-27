import seaborn as sns
import polars as pl
import matplotlib.pyplot as plt

# Parameters to change
first = False

real_radius = 3.4637199966563617

# Parameters
x_axis = "dimensions"
y_axis = ["update", "query", "memory", "ratio", "radius"]
color = "algorithm"

# File to read from
file_name = "experiments_results/perfect.csv"
output_file = "graphs/PERFECT"

COLORS = sns.color_palette()
PALETTE = {
    "OursOblivious 0.5": COLORS[0],
    "Ours 0.5": COLORS[1],
    "JONES": COLORS[2],
    "CHEN": COLORS[3],
    "OursOblivious 2.0": COLORS[4],
    "Ours 2.0": COLORS[5],
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


def read_and_plot(output_file_path):
    if first:
        replace_dots_with_commas(file_name)
    df = pl.read_csv(source=file_name, separator=";")
    df = df.with_columns(
        pl.col("algorithm").str.replace("PELLCAPPDELTA05", "OursOblivious 0.5"),
    ).with_columns(
        pl.col("algorithm").str.replace("CAPPDELTA05", "Ours 0.5")
    ).with_columns(
        pl.col("algorithm").str.replace("PELLOBL20", "NotFairOblivious 2.0"),
    ).with_columns(
        pl.col("algorithm").str.replace("PELL20", "NotFair 2.0")
    ).with_columns(
        pl.col("algorithm").str.replace("PELLCAPPDELTA20", "OursOblivious 2.0"),
    ).with_columns(
        pl.col("algorithm").str.replace("CAPPDELTA20", "Ours 2.0")
    ).with_columns(
        (df['radius']/real_radius).alias('ratio')
    )
    for graph in y_axis:
        g = sns.FacetGrid(df, col="dataset", sharex=False, sharey=False, aspect=1.5)
        hue_order = ["JONES", "CHEN", "OursOblivious 0.5", 
                     "Ours 0.5",  
                     "OursOblivious 2.0", 
                      "Ours 2.0"]
        g.map_dataframe(
            sns.barplot,  #barplot or lineplot
            x    = x_axis,   #x axis
            y    = graph, #y axis
            hue  = color, #color
            #marker="o",
            linewidth=3,
            hue_order = hue_order,
            legend="brief",
            palette = PALETTE
            )
        g.add_legend()
        #plt.gcf().set_size_inches(8, 5)
        plt.savefig(output_file_path+"_"+graph+".png", bbox_inches='tight')

# USE
read_and_plot(output_file)