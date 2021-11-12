# import foreign modules
from tensorflow.keras.datasets import mnist
import keras.utils.np_utils
import requests
import os
import tarfile
import cv2
import numpy as np
from skimage import transform


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


# Parse Chars74K dataset.
def parseDatasetChars():
    data = []
    labels = []
    for directory in os.listdir("Data/Dataset/English/Fnt"):
        label = int(directory[-3:]) - 1
        if label in range(0, 10):
            for file in os.listdir("Data/Dataset/English/Fnt/" + directory):
                data.append(cv2.imread("Data/Dataset/English/Fnt/" + directory + "/" + file))
                labels.append(label)
    return data, np.array(labels)


# Resize Chars74K images to match MNIST.
def resizeImages(images):
    for i in range(len(images)):
        images[i] = np.array(transform.resize(images[i], (28, 28), mode="constant"))
    return np.array(images)


# Converts RGB images to Grayscale.
def convertToGrayscale(images):
    grayscale = np.zeros(images.shape[:-1])
    for i in range(images.shape[0]): 
        grayscale[i] = cv2.cvtColor(images[i].astype('float32'), cv2.COLOR_RGB2GRAY)
    return grayscale


# Reshape data to four dimensions and preform categorical on labels.
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
