from preprocessing import (
    downloadDataset,
    reshapeDataset,
    normalizeData
)


# download MNIST dataset
dataset = downloadDataset()
xTrain, yTrain, xTest, yTest = reshapeDataset(dataset)
xTrain, xTest = normalizeData(xTrain, xTest)
