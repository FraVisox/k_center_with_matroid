import csv
from math import sin, cos, pi

angle = pi/3

def rotate_axis(vec, axis):
    return [vec[axis[0]]*cos(angle)-vec[axis[1]]*sin(angle), vec[axis[0]]*sin(angle)+vec[axis[1]]*cos(angle)]

def rotate(vec):
    for i in range(len(vec)-1):
        new_vec = rotate_axis(vec, [i, i+1])
        vec[i] = new_vec[0]
        vec[i+1] = new_vec[1]
    return vec

def create_first_dataset():
    with open('../../../../data/randomized/Phones_accelerometer.csv', 'r') as csvfile:
        reader = csv.reader(csvfile, delimiter=',')
        with open('../../../../data/randomized/phones_3.csv', 'w') as f:
            j = 0
            for row in reader:
                if (j == 0):
                    j += 1
                    continue
                if (j == 100000):
                    break
                for i in range(3):
                    f.write(row[i+3]+";")
                f.write(row[9]+";\n")
                j += 1

def create_other_datasets():
    new_dims = [3,6,9,12]

    for add_dim in new_dims:
        with open('../../../../data/randomized/phones_3.csv', 'r') as csvfile:
            reader = csv.reader(csvfile, delimiter=';')
            tot_dims = add_dim + 3
            with open('../../../../data/randomized/phones_'+str(tot_dims)+'.csv', 'w') as f:
                j = 0
                for row in reader:
                    vector = [float(row[i]) for i in range(3)]
                    vector.extend(0 for i in range(3, tot_dims))

                    rotated = rotate(vector)
                    for i in range(tot_dims):
                        f.write(f"{rotated[i]:.15f};")
                    f.write(row[3]+";\n")
                    j += 1

def calc_dist(vec):
    dist = 0
    for i in range(len(vec)):
        dist += vec[i]*vec[i]
    return dist

def test_rotation():
    new_dims = [3,6,9,12]

    for add_dim in new_dims:
        with open('../../../../data/randomized/phones_3.csv', 'r') as csvfile:
            reader = csv.reader(csvfile, delimiter=';')
            tot_dims = add_dim + 3
            with open('../../../../data/randomized/phones_'+str(tot_dims)+'.csv', 'r') as f:
                other_reader = csv.reader(f, delimiter=';')
                j = 0
                for row in reader:
                    vector = [float(row[i]) for i in range(3)]

                    dist = calc_dist(vector)
                    
                    vector2 = other_reader.__next__()
                    vector2 = [float(vector2[i]) for i in range(tot_dims)]
                    dist2 = calc_dist(vector2)
                    if (dist - dist2 > 1e-6):
                        print(dist, dist2)
                        
                    j += 1
        print("Finished", add_dim)
        
print("Started")
create_first_dataset()
print("Create datasets")
create_other_datasets()
print("Test")
test_rotation()