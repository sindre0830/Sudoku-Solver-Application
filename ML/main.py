# import local modules
from preprocessing import (
    downloadDatasetMNIST,
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
xTrainMNIST, yTrainMNIST, xTestMNIST, yTestMNIST = downloadDatasetMNIST()
xTrainMNIST, yTrainMNIST = reshapeDataset(xTrainMNIST, yTrainMNIST)
xTestMNIST, yTestMNIST = reshapeDataset(xTestMNIST, yTestMNIST)
xTrainMNIST = normalizeData(xTrainMNIST)
xTestMNIST = normalizeData(xTestMNIST)


# generate sequential model and output model summary
modelMNIST = generateModel()
modelMNIST.summary()


# train model
modelMNIST, resultsMNIST = trainModel(modelMNIST, xTrainMNIST, yTrainMNIST, xTestMNIST, yTestMNIST)


# analyze model and plot results
analyzeModel(modelMNIST, xTestMNIST, yTestMNIST)
plotResults(resultsMNIST)


# prompt user to save model
saveModel(modelMNIST)
