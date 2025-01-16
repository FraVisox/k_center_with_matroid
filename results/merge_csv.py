import polars as pl

output_file = "experiments_results/merged.csv"

name_of_columns = [
    "dataset", 
    "algorithm", "update", "query", "radius", "ratio", "memory",
    "dimensions", "type", "delta", "wsize", "beta", "k"
]

file_names = [
    "doubling_dimension_jones",
    "doubling_dimension_20000",
    "wsize_jones_covtype",
    "wsize_jones_higgs",
    "wsize_jones_phones",
    "beta_covtype",
    "beta_higgs",
    "beta_phones",
    "beta_normalized",
    "k_covtype",
    "k_higgs",
    "k_phones",
    "k_normalized",
    "doubling_dimension",
    "type_covtype",
    "type_higgs",
    "type_phones",    
    "type_normalized",
    "wsize_covtype",
    "wsize_higgs",
    "wsize_phones",
    "wsize_normalized"
]

replace_comma = False

#File to read from

def add_columns(df, file_name):
    if file_name == "doubling_dimension_jones":
        df = df.with_columns(
            dataset = pl.lit("blobs").alias("dataset")
        ).with_columns(
            type = pl.lit("Rand").alias("type")
        ).with_columns(
            beta = pl.lit(2.0).alias("beta")
        ).with_columns(
            k = pl.lit(21).alias("k")
        ).with_columns(
            wsize = pl.lit(10000).cast(pl.Int64).alias("wsize")
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CHEN").or_(pl.col("algorithm").str.contains("JONES"))).then(0.0).otherwise(pl.col("algorithm").str.extract(r"DELTA(\d+)").cast(pl.Float64)).alias("delta") / 10
        )
    elif file_name == "doubling_dimension_20000":
        df = df.with_columns(
            dataset = pl.lit("blobs").alias("dataset")
        ).with_columns(
            type = pl.lit("Rand").alias("type")
        ).with_columns(
            beta = pl.lit(2.0).alias("beta")
        ).with_columns(
            k = pl.lit(21).alias("k")
        ).with_columns(
            wsize = pl.lit(20000).cast(pl.Int64).alias("wsize")
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CHEN").or_(pl.col("algorithm").str.contains("JONES"))).then(0.0).otherwise(pl.col("algorithm").str.extract(r"DELTA(\d+)").cast(pl.Float64)).alias("delta") / 10
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CAPP")).then(pl.concat_str([pl.col("algorithm") ,
            pl.lit("CHEN")], separator="_")).otherwise(pl.col("algorithm")).alias("algorithm")
        )
        df = df.rename({"dim": "dimensions"})
    elif file_name == "wsize_jones_covtype":
        df = df.with_columns(
            dataset = pl.lit("covtype").alias("dataset")
        ).with_columns(
            dimensions = pl.lit(54).alias("dimensions")
        ).with_columns(
            type = pl.lit("Rand").alias("type")
        ).with_columns(
            beta = pl.lit(2.0).alias("beta")
        ).with_columns(
            k = pl.lit(14).alias("k")
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CHEN").or_(pl.col("algorithm").str.contains("JONES"))).then(0.0).otherwise(pl.col("algorithm").str.extract(r"DELTA(\d+)").cast(pl.Float64)).alias("delta") / 10
        )
    elif file_name == "wsize_jones_higgs":
        df = df.with_columns(
            dataset = pl.lit("higgs").alias("dataset")
        ).with_columns(
            dimensions = pl.lit(7).alias("dimensions")
        ).with_columns(
            type = pl.lit("Rand").alias("type")
        ).with_columns(
            beta = pl.lit(2.0).alias("beta")
        ).with_columns(
            k = pl.lit(4).alias("k")
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CHEN").or_(pl.col("algorithm").str.contains("JONES"))).then(0.0).otherwise(pl.col("algorithm").str.extract(r"DELTA(\d+)").cast(pl.Float64)).alias("delta") / 10
        )
    elif file_name == "wsize_jones_phones":
        df = df.with_columns(
            dataset = pl.lit("phones").alias("dataset")
        ).with_columns(
            dimensions = pl.lit(3).alias("dimensions")
        ).with_columns(
            type = pl.lit("Rand").alias("type")
        ).with_columns(
            beta = pl.lit(2.0).alias("beta")
        ).with_columns(
            k = pl.lit(14).alias("k")
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CHEN").or_(pl.col("algorithm").str.contains("JONES"))).then(0.0).otherwise(pl.col("algorithm").str.extract(r"DELTA(\d+)").cast(pl.Float64)).alias("delta") / 10
        )
    elif file_name == "beta_covtype":
        df = df.with_columns(
            dataset = pl.lit("covtype").alias("dataset")
        ).with_columns(
            dimensions = pl.lit(54).alias("dimensions")
        ).with_columns(
            type = pl.lit("Rand").alias("type")
        ).with_columns(
            k = pl.lit(14).alias("k")
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CHEN").or_(pl.col("algorithm").str.contains("JONES"))).then(0.0).otherwise(pl.col("algorithm").str.extract(r"DELTA(\d+)").cast(pl.Float64)).alias("delta") / 10
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CAPP")).then(pl.concat_str([pl.col("algorithm") ,
            pl.lit("CHEN")], separator="_")).otherwise(pl.col("algorithm")).alias("algorithm")
        ).with_columns(
            wsize = pl.lit(10000).cast(pl.Int64).alias("wsize")
        )
    elif file_name == "beta_higgs":
        df = df.with_columns(
            dataset = pl.lit("higgs").alias("dataset")
        ).with_columns(
            dimensions = pl.lit(7).alias("dimensions")
        ).with_columns(
            type = pl.lit("Rand").alias("type")
        ).with_columns(
            k = pl.lit(4).alias("k")
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CHEN").or_(pl.col("algorithm").str.contains("JONES"))).then(0.0).otherwise(pl.col("algorithm").str.extract(r"DELTA(\d+)").cast(pl.Float64)).alias("delta") / 10
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CAPP")).then(pl.concat_str([pl.col("algorithm") ,
            pl.lit("CHEN")], separator="_")).otherwise(pl.col("algorithm")).alias("algorithm")
        ).with_columns(
            wsize = pl.lit(10000).cast(pl.Int64).alias("wsize")
        )
    elif file_name == "beta_phones":
        df = df.with_columns(
            dataset = pl.lit("phones").alias("dataset")
        ).with_columns(
            dimensions = pl.lit(3).alias("dimensions")
        ).with_columns(
            type = pl.lit("Rand").alias("type")
        ).with_columns(
            k = pl.lit(14).alias("k")
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CHEN").or_(pl.col("algorithm").str.contains("JONES"))).then(0.0).otherwise(pl.col("algorithm").str.extract(r"DELTA(\d+)").cast(pl.Float64)).alias("delta") / 10
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CAPP")).then(pl.concat_str([pl.col("algorithm") ,
            pl.lit("CHEN")], separator="_")).otherwise(pl.col("algorithm")).alias("algorithm")
        ).with_columns(
            wsize = pl.lit(10000).cast(pl.Int64).alias("wsize")
        )
    elif file_name == "beta_normalized":
        df = df.with_columns(
            dataset = pl.lit("normalized").alias("dataset")
        ).with_columns(
            dimensions = pl.lit(54).alias("dimensions")
        ).with_columns(
            type = pl.lit("Rand").alias("type")
        ).with_columns(
            k = pl.lit(14).alias("k")
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CHEN").or_(pl.col("algorithm").str.contains("JONES"))).then(0.0).otherwise(pl.col("algorithm").str.extract(r"DELTA(\d+)").cast(pl.Float64)).alias("delta") / 10
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CAPP")).then(pl.concat_str([pl.col("algorithm") ,
            pl.lit("CHEN")], separator="_")).otherwise(pl.col("algorithm")).alias("algorithm")
        ).with_columns(
            wsize = pl.lit(10000).cast(pl.Int64).alias("wsize")
        )
    elif file_name == "doubling_dimension":
        df = df.with_columns(
            dataset = pl.lit("blobs").alias("dataset")
        ).with_columns(
            type = pl.lit("Rand").alias("type")
        ).with_columns(
            k = pl.lit(21).alias("k")
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CHEN").or_(pl.col("algorithm").str.contains("JONES"))).then(0.0).otherwise(pl.col("algorithm").str.extract(r"DELTA(\d+)").cast(pl.Float64)).alias("delta") / 10
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CAPP")).then(pl.concat_str([pl.col("algorithm") ,
            pl.lit("CHEN")], separator="_")).otherwise(pl.col("algorithm")).alias("algorithm")
        ).with_columns(
            wsize = pl.lit(10000).cast(pl.Int64).alias("wsize")
        ).with_columns(
            beta = pl.lit(2.0).alias("beta")
        )
    elif file_name == "k_covtype":
        df = df.with_columns(
            dataset = pl.lit("covtype").alias("dataset")
        ).with_columns(
            dimensions = pl.lit(54).alias("dimensions")
        ).with_columns(
            type = pl.lit("Rand").alias("type")
        ).with_columns(
            beta = pl.lit(2.0).alias("beta")
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CHEN").or_(pl.col("algorithm").str.contains("JONES"))).then(0.0).otherwise(pl.col("algorithm").str.extract(r"DELTA(\d+)").cast(pl.Float64)).alias("delta") / 10
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CAPP")).then(pl.concat_str([pl.col("algorithm") ,
            pl.lit("CHEN")], separator="_")).otherwise(pl.col("algorithm")).alias("algorithm")
        ).with_columns(
            wsize = pl.lit(10000).cast(pl.Int64).alias("wsize")
        )
    elif file_name == "k_higgs":
        df = df.with_columns(
            dataset = pl.lit("higgs").alias("dataset")
        ).with_columns(
            dimensions = pl.lit(7).alias("dimensions")
        ).with_columns(
            type = pl.lit("Rand").alias("type")
        ).with_columns(
            beta = pl.lit(2.0).alias("beta")
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CHEN").or_(pl.col("algorithm").str.contains("JONES"))).then(0.0).otherwise(pl.col("algorithm").str.extract(r"DELTA(\d+)").cast(pl.Float64)).alias("delta") / 10
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CAPP")).then(pl.concat_str([pl.col("algorithm") ,
            pl.lit("CHEN")], separator="_")).otherwise(pl.col("algorithm")).alias("algorithm")
        ).with_columns(
            wsize = pl.lit(10000).cast(pl.Int64).alias("wsize")
        )
    elif file_name == "k_phones":
        df = df.with_columns(
            dataset = pl.lit("phones").alias("dataset")
        ).with_columns(
            dimensions = pl.lit(3).alias("dimensions")
        ).with_columns(
            type = pl.lit("Rand").alias("type")
        ).with_columns(
            beta = pl.lit(2.0).alias("beta")
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CHEN").or_(pl.col("algorithm").str.contains("JONES"))).then(0.0).otherwise(pl.col("algorithm").str.extract(r"DELTA(\d+)").cast(pl.Float64)).alias("delta") / 10
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CAPP")).then(pl.concat_str([pl.col("algorithm") ,
            pl.lit("CHEN")], separator="_")).otherwise(pl.col("algorithm")).alias("algorithm")
        ).with_columns(
            wsize = pl.lit(10000).cast(pl.Int64).alias("wsize")
        )
    elif file_name == "k_normalized":
        df = df.with_columns(
            dataset = pl.lit("normalized").alias("dataset")
        ).with_columns(
            dimensions = pl.lit(54).alias("dimensions")
        ).with_columns(
            type = pl.lit("Rand").alias("type")
        ).with_columns(
            beta = pl.lit(2.0).alias("beta")
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CHEN").or_(pl.col("algorithm").str.contains("JONES"))).then(0.0).otherwise(pl.col("algorithm").str.extract(r"DELTA(\d+)").cast(pl.Float64)).alias("delta") / 10
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CAPP")).then(pl.concat_str([pl.col("algorithm") ,
            pl.lit("CHEN")], separator="_")).otherwise(pl.col("algorithm")).alias("algorithm")
        ).with_columns(
            wsize = pl.lit(10000).cast(pl.Int64).alias("wsize")
        )
    elif file_name == "type_covtype":
        df = df.with_columns(
            dataset = pl.lit("covtype").alias("dataset")
        ).with_columns(
            dimensions = pl.lit(54).alias("dimensions")
        ).with_columns(
            k = pl.lit(14).alias("k")
        ).with_columns(
            beta = pl.lit(2.0).alias("beta")
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CHEN").or_(pl.col("algorithm").str.contains("JONES"))).then(0.0).otherwise(pl.col("algorithm").str.extract(r"DELTA(\d+)").cast(pl.Float64)).alias("delta") / 10
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CAPP")).then(pl.concat_str([pl.col("algorithm") ,
            pl.lit("CHEN")], separator="_")).otherwise(pl.col("algorithm")).alias("algorithm")
        ).with_columns(
            wsize = pl.lit(10000).cast(pl.Int64).alias("wsize")
        )
    elif file_name == "type_higgs":
        df = df.with_columns(
            dataset = pl.lit("higgs").alias("dataset")
        ).with_columns(
            dimensions = pl.lit(7).alias("dimensions")
        ).with_columns(
            k = pl.lit(4).alias("k")
        ).with_columns(
            beta = pl.lit(2.0).alias("beta")
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CHEN").or_(pl.col("algorithm").str.contains("JONES"))).then(0.0).otherwise(pl.col("algorithm").str.extract(r"DELTA(\d+)").cast(pl.Float64)).alias("delta") / 10
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CAPP")).then(pl.concat_str([pl.col("algorithm") ,
            pl.lit("CHEN")], separator="_")).otherwise(pl.col("algorithm")).alias("algorithm")
        ).with_columns(
            wsize = pl.lit(10000).cast(pl.Int64).alias("wsize")
        )
    elif file_name == "type_phones":
        df = df.with_columns(
            dataset = pl.lit("phones").alias("dataset")
        ).with_columns(
            dimensions = pl.lit(3).alias("dimensions")
        ).with_columns(
            k = pl.lit(14).alias("k")
        ).with_columns(
            beta = pl.lit(2.0).alias("beta")
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CHEN").or_(pl.col("algorithm").str.contains("JONES"))).then(0.0).otherwise(pl.col("algorithm").str.extract(r"DELTA(\d+)").cast(pl.Float64)).alias("delta") / 10
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CAPP")).then(pl.concat_str([pl.col("algorithm") ,
            pl.lit("CHEN")], separator="_")).otherwise(pl.col("algorithm")).alias("algorithm")
        ).with_columns(
            wsize = pl.lit(10000).cast(pl.Int64).alias("wsize")
        )
    elif file_name == "type_normalized":
        df = df.with_columns(
            dataset = pl.lit("normalized").alias("dataset")
        ).with_columns(
            dimensions = pl.lit(54).alias("dimensions")
        ).with_columns(
            k = pl.lit(14).alias("k")
        ).with_columns(
            beta = pl.lit(2.0).alias("beta")
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CHEN").or_(pl.col("algorithm").str.contains("JONES"))).then(0.0).otherwise(pl.col("algorithm").str.extract(r"DELTA(\d+)").cast(pl.Float64)).alias("delta") / 10
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CAPP")).then(pl.concat_str([pl.col("algorithm") ,
            pl.lit("CHEN")], separator="_")).otherwise(pl.col("algorithm")).alias("algorithm")
        ).with_columns(
            wsize = pl.lit(10000).cast(pl.Int64).alias("wsize")
        )
    elif file_name == "wsize_covtype":
        df = df.with_columns(
            dataset = pl.lit("covtype").alias("dataset")
        ).with_columns(
            dimensions = pl.lit(54).alias("dimensions")
        ).with_columns(
            k = pl.lit(14).alias("k")
        ).with_columns(
            beta = pl.lit(2.0).alias("beta")
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CHEN").or_(pl.col("algorithm").str.contains("JONES"))).then(0.0).otherwise(pl.col("algorithm").str.extract(r"DELTA(\d+)").cast(pl.Float64)).alias("delta") / 10
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CAPP")).then(pl.concat_str([pl.col("algorithm") ,
            pl.lit("CHEN")], separator="_")).otherwise(pl.col("algorithm")).alias("algorithm")
        ).with_columns(
            type = pl.lit("Rand").alias("type")
        )
    elif file_name == "wsize_higgs":
        df = df.with_columns(
            dataset = pl.lit("higgs").alias("dataset")
        ).with_columns(
            dimensions = pl.lit(7).alias("dimensions")
        ).with_columns(
            k = pl.lit(4).alias("k")
        ).with_columns(
            beta = pl.lit(2.0).alias("beta")
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CHEN").or_(pl.col("algorithm").str.contains("JONES"))).then(0.0).otherwise(pl.col("algorithm").str.extract(r"DELTA(\d+)").cast(pl.Float64)).alias("delta") / 10
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CAPP")).then(pl.concat_str([pl.col("algorithm") ,
            pl.lit("CHEN")], separator="_")).otherwise(pl.col("algorithm")).alias("algorithm")
        ).with_columns(
            type = pl.lit("Rand").alias("type")
        )
    elif file_name == "wsize_phones":
        df = df.with_columns(
            dataset = pl.lit("phones").alias("dataset")
        ).with_columns(
            dimensions = pl.lit(3).alias("dimensions")
        ).with_columns(
            k = pl.lit(14).alias("k")
        ).with_columns(
            beta = pl.lit(2.0).alias("beta")
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CHEN").or_(pl.col("algorithm").str.contains("JONES"))).then(0.0).otherwise(pl.col("algorithm").str.extract(r"DELTA(\d+)").cast(pl.Float64)).alias("delta") / 10
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CAPP")).then(pl.concat_str([pl.col("algorithm") ,
            pl.lit("CHEN")], separator="_")).otherwise(pl.col("algorithm")).alias("algorithm")
        ).with_columns(
            type = pl.lit("Rand").alias("type")
        )
    elif file_name == "wsize_normalized":
        df = df.with_columns(
            dataset = pl.lit("normalized").alias("dataset")
        ).with_columns(
            dimensions = pl.lit(54).alias("dimensions")
        ).with_columns(
            k = pl.lit(14).alias("k")
        ).with_columns(
            beta = pl.lit(2.0).alias("beta")
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CHEN").or_(pl.col("algorithm").str.contains("JONES"))).then(0.0).otherwise(pl.col("algorithm").str.extract(r"DELTA(\d+)").cast(pl.Float64)).alias("delta") / 10
        ).with_columns(
            pl.when(pl.col("algorithm").str.contains("CAPP")).then(pl.concat_str([pl.col("algorithm") ,
            pl.lit("CHEN")], separator="_")).otherwise(pl.col("algorithm")).alias("algorithm")
        ).with_columns(
            type = pl.lit("Rand").alias("type")
        )
    

    

    for col in df.columns:
        if df[col].dtype == pl.Int32:
            df = df.with_columns(pl.col(col).cast(pl.Int64))
    df = df.select(name_of_columns)
    
    return df

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


def merge(output_file):
    dataframe = [] 
    i = 0
    for file in file_names:
        input_file = "experiments_results/"+file+".csv"
        if i > 4:
            input_file = "experiments_results/CHEN/"+file+".csv"
        if replace_comma:
            replace_dots_with_commas(input_file)
        df = pl.read_csv(source=input_file, separator=";", infer_schema_length=10000, truncate_ragged_lines=True)
        df = df.with_columns(
            pl.col("query").cast(pl.Float64).alias("query")
        )
        df = add_columns(df, file)
        dataframe.append(df)
        i += 1
    dat = pl.concat(dataframe)

    dat.write_csv(output_file, separator=";")

# USE
merge(output_file)