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
# import foreign modules
import tensorflow as tf


# enable memory growth for gpu devices
# source: https://stackoverflow.com/a/55541385/8849692
gpu_devices = tf.config.experimental.list_physical_devices('GPU')
if gpu_devices:
    for devices in gpu_devices:
        tf.config.experimental.set_memory_growth(devices, True)


# download MNIST dataset and perform preprocessing
dataset = downloadDataset()
xTrain, yTrain, xTest, yTest = reshapeDataset(dataset)
xTrain, xTest = normalizeData(xTrain, xTest)


# generate sequential model and output model summary
model = generateModel()
model.summary()


# train model
model, results = trainModel(model, xTrain, yTrain, xTest, yTest)
