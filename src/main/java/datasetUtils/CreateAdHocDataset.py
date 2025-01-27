from sklearn.datasets import make_blobs
import math
import random
# Create BLOBS dataset:
#       n_samples can be an array, one value for the number of samples of each cluster
#       n_features is the number of features (dimensions)
#       centers is the number of clusters
#       cluster_std is the standard deviation of the clusters (which means that increasing it will increase the radius)
#       center_box is the range of the centers (where the centers will be generated, which means what are the max and min values of their coordinates)
#       random_state is the seed of the random number generator (used for reproducibility)
n_dimensions = 15
X, y, centers = make_blobs(n_samples=100000, n_features=n_dimensions, centers=21, cluster_std=0.5, center_box=(0, 100), random_state=0, return_centers=True)

def find_dist(vec, center):
    dist = 0
    for i in range(len(vec)):
        dist += (vec[i]-center[i])*(vec[i]-center[i])
    return math.sqrt(dist)

# Find the radius:
radius = 0
special = 0
for i in range(len(X)):
    dist = find_dist(X[i], centers[y[i]])
    if (dist > radius):
        radius = dist
        special = i


print(radius)
# X are the samples, y are the labels of the samples (that indicate the cluster to which each sample belongs), centers are the centers of each cluster

# Write dataset to output file:
def create():
    with open('../../../../data/randomized/perfect_dataset.csv', 'w') as f:
        print("Started writing")
        j = 0
        # The first 22 points are special
        special_sample = X[special]
        for i in special_sample:
            f.write(str(i)+";")
        f.write(str(random.randint(0, 6)))
        f.write(";\n")

        for center in centers:
            for i in center:
                f.write(str(i)+";")
            f.write(str(j%7))
            f.write(";\n")
            j += 1
        
        for i in range(len(X)): 
            if (i == special):
                continue
            sample = X[i]
            for i in sample:
                f.write(str(i)+";")
            f.write(str(random.randint(0, 6)))
            f.write(";\n")
        print("Finished writing")

    