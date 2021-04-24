import matplotlib.pyplot as plt
import sys
import numpy as np

histogramR = list(map(int, sys.argv[1].strip('[]').split(',')))
x_red = np.arange(0, len(histogramR))
histogramG = list(map(int, sys.argv[2].strip('[]').split(',')))
x_green = np.arange(0, len(histogramG))
histogramB = list(map(int, sys.argv[3].strip('[]').split(',')))
x_blue = np.arange(0, len(histogramB))

print(x_red)
print(histogramR)

print(x_green)
print(histogramG)

print(x_blue)
print(histogramB)

fig, ax = plt.subplots(1,3)
fig.tight_layout()
ax[0].bar(x_red, histogramR)
ax[0].set_title("Red Channel")
ax[1].bar(x_green, histogramG)
ax[1].set_title("Green Channel")
ax[2].bar(x_blue, histogramB)
ax[2].set_title("Blue Channel")
plt.show()