import seaborn as sns
import polars as pl
import matplotlib.pyplot as plt

#File to read from
first = True
type_of_graph = "doubling_dimension_jones"
file_name = "experiments_results/"+type_of_graph+".csv"
output_file = "graphs/"+type_of_graph

#Parameters
x_axis = "dimension"
y_axis = ["update", "query", "radius", "ratio", "memory"]
color = 'algorithm'

pal = dict(
    JONES="#f22020", #red 
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
    #CAPPDELTA05="#228c68", #dark green
    CAPPDELTA10="#00E5EE", #light blue
    CAPPDELTA15="#f07cab", #pink
    CAPPDELTA20="#000000", #black
    )


def replace_dots_with_commas(file_path):
    try:
        # Read the content of the file
        with open(file_path, 'r') as file:
            content = file.read()

        # Replace all commas with dots
        modified_content = content.replace(',', '.')

        # Write the modified content back to the file
        with open(file_path, 'w') as file:
            file.write(modified_content)

        print("Commas have been replaced with dots successfully.")

    except FileNotFoundError:
        print(f"The file at {file_path} was not found.")
    except Exception as e:
        print(f"An error occurred: {e}")

def read_and_plot(output_file_path):
    if first:
        replace_dots_with_commas(file_name)
    df = pl.read_csv(source=file_name, separator=";")
    df = df.with_columns(
        pl.lit("blobs").alias("dataset")
    )
    for graph in y_axis:
        g = sns.FacetGrid(df, col="dataset", sharex=False, sharey=False, aspect=1.5)
        g.map_dataframe(
            sns.lineplot,  #barplot or lineplot
            x    = x_axis,   #x axis
            y    = graph, #y axis
            hue  = color, #color
            marker="o",
            palette=pal,
            linewidth=3,
            hue_order = ["CAPPDELTA20", "CAPPDELTA15", "CAPPDELTA10", "CAPPDELTA05","PELLCAPPDELTA20", "PELLCAPPDELTA15", "PELLCAPPDELTA10", "PELLCAPPDELTA05",  "PELLCAPP", "JONES"]
            )
        g.add_legend()
        #plt.gcf().set_size_inches(8, 5)
        plt.savefig(output_file_path+"_"+graph+".png", bbox_inches='tight')

# USE
read_and_plot(output_file)