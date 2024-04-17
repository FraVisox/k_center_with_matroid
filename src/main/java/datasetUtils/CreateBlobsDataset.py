from sklearn.datasets import make_blobs
import random
# Create dataset: 
#       n_samples can be an array, one value for the number of samples of each cluster
#       n_features is the number of features (dimensions)
#       centers is the number of clusters
#       cluster_std is the standard deviation of the clusters (which means that increasing it will increase the radius)
#       center_box is the range of the centers (where the centers will be generated, which means what are the max and min values of their coordinates)
#       random_state is the seed of the random number generator (used for reproducibility)
n_dimensions = 25
X, y = make_blobs(n_samples=1000000, n_features=n_dimensions, centers=21, cluster_std=2, center_box=(0, 100), random_state=0)

# X are the samples, y are the labels of the samples (that indicate the cluster to which each sample belongs), centers are the centers of each cluster

# Write dataset to output file:
with open('../../../../data/randomized/blobs'+str(n_dimensions)+'.csv', 'w') as f:
    print("Started writing")
    for sample in X:
        for i in sample:
            f.write((str(i)+";").replace(".",","))
        f.write(str(random.randint(0, 7)))
        f.write(";\n")
    print("Finished writing")

    