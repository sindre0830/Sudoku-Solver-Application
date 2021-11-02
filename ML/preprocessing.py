from tensorflow.keras.datasets import mnist
import keras.utils.np_utils


# Download MNIST dataset.
def downloadDataset():
    return mnist.load_data()


# Reshape data to four dimensions and perform categorical on labels.
def reshapeDataset(dataset):
    (xTrain, yTrain), (xTest, yTest) = dataset
    xTrain = xTrain.reshape(xTrain.shape[0], xTrain.shape[1], xTrain.shape[2], 1).astype('float32')
    xTest = xTest.reshape(xTest.shape[0], xTest.shape[1], xTest.shape[2], 1).astype('float32')
    yTrain = keras.utils.np_utils.to_categorical(yTrain)
    yTest = keras.utils.np_utils.to_categorical(yTest)
    return xTrain, yTrain, xTest, yTest


# Normalize data between 0 and 1.
def normalizeData(xTrain, xTest):
    xTrain = xTrain / 255.0
    xTest = xTest / 255.0
    return xTrain, xTest
