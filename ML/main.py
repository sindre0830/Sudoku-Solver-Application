from preprocessing import (
    downloadDataset,
    reshapeDataset
)


# download MNIST dataset
xTrain, xTest, yTrain, yTest = downloadDataset()
xTrain, xTest, yTrain, yTest = reshapeDataset(xTrain, xTest, yTrain, yTest)
