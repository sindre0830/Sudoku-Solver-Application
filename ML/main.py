# import local modules
from preprocessing import (
    downloadDataset,
    reshapeDataset,
    normalizeData
)
from model import (
    generateModel
)


# download MNIST dataset
dataset = downloadDataset()
xTrain, yTrain, xTest, yTest = reshapeDataset(dataset)
xTrain, xTest = normalizeData(xTrain, xTest)


# generate sequential model
model = generateModel()
model.summary()
