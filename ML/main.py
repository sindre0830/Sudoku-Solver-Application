from preprocessing import (
    downloadDataset,
    reshapeDataset
)


# download MNIST dataset
dataset = downloadDataset()
dataset = reshapeDataset(dataset)
