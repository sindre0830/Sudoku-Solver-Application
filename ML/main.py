# import local modules
from preprocessing import (
    downloadDataset,
    reshapeDataset,
    normalizeData
)
from model import (
    generateModel,
    trainModel
)


# download MNIST dataset and perform preprocessing
dataset = downloadDataset()
xTrain, yTrain, xTest, yTest = reshapeDataset(dataset)
xTrain, xTest = normalizeData(xTrain, xTest)


# generate sequential model and output model summary
model = generateModel()
model.summary()


# train model
model, results = trainModel(model, xTrain, yTrain, xTest, yTest)
