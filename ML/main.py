# import local modules
from preprocessing import (
    downloadDatasetMNIST,
    downloadDatasetChars,
    reshapeDataset,
    normalizeData
)
from model import (
    generateModel,
    trainModel,
    analyzeModel,
    plotResults,
    saveModel
)
# import foreign modules
import os
import tensorflow as tf


# suppress info and warnings outputted by tensorflow
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3'
tf.compat.v1.logging.set_verbosity(tf.compat.v1.logging.ERROR)
# enable memory growth for gpu devices
# source: https://stackoverflow.com/a/55541385/8849692
gpu_devices = tf.config.experimental.list_physical_devices('GPU')
if gpu_devices:
    for devices in gpu_devices:
        tf.config.experimental.set_memory_growth(devices, True)


# download MNIST dataset and perform preprocessing
dataset = downloadDatasetMNIST()
downloadDatasetChars()
xTrain, yTrain, xTest, yTest = reshapeDataset(dataset)
xTrain, xTest = normalizeData(xTrain, xTest)


# generate sequential model and output model summary
model = generateModel()
model.summary()


# train model
model, results = trainModel(model, xTrain, yTrain, xTest, yTest)


# analyze model and plot results
analyzeModel(model, xTest, yTest)
plotResults(results)


# prompt user to save model
saveModel(model)
