# import foreign modules
from tensorflow.keras.datasets import mnist
import keras.utils.np_utils
import requests
import os
import tarfile


# Download MNIST dataset.
def downloadDatasetMNIST():
    return mnist.load_data()


# Download Chars74K dataset.
def downloadDatasetChars():
    if os.path.exists('Data/Dataset/English') is not True:
        os.mkdir('Data/Dataset')
        req = requests.get('http://www.ee.surrey.ac.uk/CVSSP/demos/chars74k/EnglishFnt.tgz', allow_redirects=True)
        open('Data/Dataset/compressedData.tgz', 'wb').write(req.content)
        with tarfile.open('Data/Dataset/compressedData.tgz', 'r:*') as file:
            file.extractall('Data/Dataset')
            file.close()
        os.remove('Data/Dataset/compressedData.tgz')


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
