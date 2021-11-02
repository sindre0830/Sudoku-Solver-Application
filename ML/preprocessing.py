from tensorflow.keras.datasets import mnist
import keras.utils.np_utils


# Download MNIST dataset.
def downloadDataset():
    (xTrain, yTrain), (xTest, yTest) = mnist.load_data()
    return xTrain, xTest, yTrain, yTest


# Reshape data to four dimensions and perform categorical on labels.
def reshapeDataset(xTrain, xTest, yTrain, yTest):
    xTrain = xTrain.reshape(xTrain.shape[0], xTrain.shape[1], xTrain.shape[2], 1).astype('float32')
    xTest = xTest.reshape(xTest.shape[0], xTest.shape[1], xTest.shape[2], 1).astype('float32')
    yTrain = keras.utils.np_utils.to_categorical(yTrain)
    yTest = keras.utils.np_utils.to_categorical(yTest)
    return xTrain, xTest, yTrain, yTest
