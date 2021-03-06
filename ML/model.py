# import local modules
import dictionary as dict
# import foreign modules
import keras.models
import keras.layers.convolutional
import keras.layers.pooling
import keras.layers.core
import keras.layers
import keras.regularizers
import numpy as np
import sklearn.metrics
import matplotlib.pyplot as plt
import tensorflow as tf
from tensorflow.keras.utils import plot_model


# Generate convolutional neural network model 1.
def generateModel1():
    # source: https://medium.com/analytics-vidhya/deep-learning-project-handwritten-digit-recognition-using-python-26da7ed11d1c
    model = keras.models.Sequential([
        # layer 1
        keras.layers.convolutional.Conv2D(
            filters=32, kernel_size=3, activation='relu',
            input_shape=(dict.IMAGE_SIZE, dict.IMAGE_SIZE, 1), kernel_initializer='he_uniform'
        ),
        keras.layers.pooling.MaxPooling2D(pool_size=(2, 2)),
        # layer 2
        keras.layers.convolutional.Conv2D(filters=64, kernel_size=3, activation='relu', kernel_initializer='he_uniform'),
        # layer 3
        keras.layers.convolutional.Conv2D(filters=64, kernel_size=3, activation='relu', kernel_initializer='he_uniform'),
        keras.layers.pooling.MaxPooling2D(pool_size=(2, 2)),
        # flatten
        keras.layers.core.Flatten(),
        # layer 4
        keras.layers.core.Dense(units=100, activation='relu', kernel_initializer='he_uniform'),
        # layer 5
        keras.layers.core.Dense(units=10, activation='softmax')
    ])
    model.compile(
        optimizer='adam',
        loss='categorical_crossentropy',
        metrics=['accuracy']
    )
    return model


# Generate convolutional neural network model 2.
def generateModel2():
    # source: https://towardsdatascience.com/going-beyond-99-mnist-handwritten-digits-recognition-cfff96337392
    model = keras.models.Sequential([
        # layer 1
        keras.layers.convolutional.Conv2D(
            filters=32, kernel_size=5, strides=1, activation='relu',
            input_shape=(dict.IMAGE_SIZE, dict.IMAGE_SIZE, 1), kernel_regularizer=keras.regularizers.l2(0.0005)
        ),
        # layer 2
        keras.layers.convolutional.Conv2D(filters=32, kernel_size=5, strides=1, use_bias=False),
        # layer 3
        keras.layers.BatchNormalization(),
        keras.layers.Activation('relu'),
        keras.layers.pooling.MaxPooling2D(pool_size=2, strides=2),
        keras.layers.core.Dropout(0.25),
        # layer 3
        keras.layers.convolutional.Conv2D(filters=64, kernel_size=3, strides=1, activation='relu', kernel_regularizer=keras.regularizers.l2(0.0005)),
        # layer 4
        keras.layers.convolutional.Conv2D(filters=64, kernel_size=3, strides=1, use_bias=False),
        # layer 5
        keras.layers.BatchNormalization(),
        keras.layers.Activation('relu'),
        keras.layers.pooling.MaxPooling2D(pool_size=2, strides=2),
        keras.layers.core.Dropout(0.25),
        # flatten
        keras.layers.core.Flatten(),
        # layer 6
        keras.layers.core.Dense(units=256, use_bias=False),
        # layer 7
        keras.layers.BatchNormalization(),
        keras.layers.core.Activation('relu'),
        # layer 8
        keras.layers.core.Dense(units=128, use_bias=False),
        # layer 9
        keras.layers.BatchNormalization(),
        keras.layers.core.Activation('relu'),
        # layer 10
        keras.layers.core.Dense(units=84, use_bias=False),
        # layer 11
        keras.layers.BatchNormalization(),
        keras.layers.core.Activation('relu'),
        keras.layers.core.Dropout(0.25),
        # output
        keras.layers.core.Dense(units=10, activation='softmax')
    ])
    model.compile(
        optimizer='adam',
        loss='categorical_crossentropy',
        metrics=['accuracy']
    )
    return model


# Train model and evaluate when finished.
def trainModel(model: keras.models.Sequential, xTrain, yTrain, xTest, yTest):
    # fit model by parameters
    results = model.fit(
        xTrain,
        yTrain,
        batch_size=dict.BATCH_SIZE,
        epochs=dict.EPOCHS,
        verbose=False,
        validation_data=(xTest, yTest)
    )
    # evaluate model and output results
    model.evaluate(xTest, yTest)
    return model, results


# Analyze model on testing data and output classification report and confusion matrix.
def analyzeModel(model: keras.models.Sequential, xTest, yTest):
    # predict testing data on model
    yPred = model.predict(xTest)
    # flatten each array to get index of highest value
    yPred = np.argmax(yPred, axis=1)
    yTest = np.argmax(yTest, axis=1)
    # print classification report and confusion matrix
    print(sklearn.metrics.classification_report(yTest, yPred, zero_division=1))
    print(sklearn.metrics.confusion_matrix(yTest, yPred))


# Plots a graph with the results from model training.
def plotResults(results):
    # get values from results
    history_dict = results.history
    loss_values = history_dict['loss']
    val_loss_values = history_dict['val_loss']
    val_accuracy = history_dict['val_accuracy']
    epochs = range(1, (len(history_dict['loss']) + 1))
    # plot training and validation loss
    plt.clf()
    plt.plot(epochs, loss_values, label='Training loss', c='lightgreen')
    plt.plot(epochs, val_loss_values, label='Validation loss')
    plt.title('Training and validation loss')
    plt.xlabel('Epochs')
    plt.ylabel('Loss')
    plt.legend()
    plt.show()
    # plot validation accuracy
    plt.clf()
    plt.plot(epochs, val_accuracy, label='Validation accuracy', c='red')
    plt.title('Validation accuracy')
    plt.xlabel('Epochs')
    plt.ylabel('Accuracy')
    plt.legend()
    plt.show()


# Ask user to save model to disk in HDF5 and tflite format.
def saveModel(model: keras.models.Sequential, filename: str):
    inp = input("Do you want to save the model? Y/N: ")
    if inp.lower() == "y":
        # save model in HDF5 format
        model.save("Data/" + filename + ".h5")
        # convert model to tensorflow lite and save it
        # source: https://www.tensorflow.org/lite/convert/
        converter = tf.lite.TFLiteConverter.from_keras_model(model)
        tflite_model = converter.convert()
        with open("Data/" + filename + ".tflite", "wb") as file:
            file.write(tflite_model)


# Plot model and save as PNG.
def plotModel(model: keras.models.Sequential, filename: str):
    plot_model(model, to_file="Data/" + filename + ".png", show_shapes=True)
