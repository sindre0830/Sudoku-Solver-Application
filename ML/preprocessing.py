from tensorflow.keras.datasets import mnist


# Download MNIST dataset.
def downloadDataset():
    (xTrain, yTrain), (xTest, yTest) = mnist.load_data()
    return xTrain, xTest, yTrain, yTest
