# import local modules
import dictionary as dict
# import foreign modules
import keras.models
import keras.layers.convolutional
import keras.layers.pooling
import keras.layers.core
import numpy as np
import sklearn.metrics


# Generate convolutional neural network model.
def generateModel():
    # source: https://medium.com/analytics-vidhya/deep-learning-project-handwritten-digit-recognition-using-python-26da7ed11d1c
    model = keras.models.Sequential([
        # layer 1
        keras.layers.convolutional.Conv2D(filters=32, kernel_size=3, input_shape=(28, 28, 1), activation='relu', kernel_initializer='he_uniform'),
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
