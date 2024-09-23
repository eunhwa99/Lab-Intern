from scipy.interpolate import splrep, splev
import numpy as np
import matplotlib.pyplot as plt
import math

datalistx = []


def read_data(filename):
    f = open(filename, 'r')
    lines = f.readlines()
    cnt = 0
    for line in lines:
        a, b = line.split(",")
        datalistx.append((cnt, float(a)))
        cnt = cnt+1


read_data("accang02.txt")

datalistx = np.asarray(datalistx)
print(datalistx)
plt.figure()
plt.scatter(datalistx[:,0], datalistx[:,1])
plt.show()

spl = splrep(datalistx[:,0], datalistx[:,1])
fs = 100
dt = 1/fs
spl = splrep(datalistx[:,0], datalistx[:,1])
newt = np.arange(0, 180, dt)
newx = splev(newt, spl)
print(newt)
print(newx)
if True:
    plt.figure()
    plt.scatter(newt, newx)
    plt.show()

nfft = len(newt)
print('nfft = ', nfft)
df = fs/nfft
k = np.arange(nfft)
f = k * df

nfft_half = math.trunc(nfft/2)
f0 = f[range(nfft_half)]
y = np.fft.fft(newx)/nfft * 2
y0 = y[range(nfft_half)]
amp = abs(y0)
if True:
    plt.figure()
    plt.scatter(f0, amp)
    plt.show()

ampsort = np.sort(amp)
outer = ampsort[:1000]
print('outer cnt=', len(outer))
topn = len(outer)
print(outer)


idxy = np.argsort(-amp)
for i in range(topn):
    print('freq = ', f0[idxy[i]], 'amp=', y[idxy[i]])

newy = np.zeros((nfft,))
for i in range(topn):
    freq = f0[idxy[i]]
    yx = y[idxy[i]]
    coec = yx.real
    coes = yx.imag * -1
    # print('freq=', freq, 'coec=', coec, ' coes', coes)
    # newy += coec * np.cos( 2*np.pi*freq*newt+ang) + coes * np.sin( 2*np.pi*freq*newt+ang)
    newy += coec * np.cos(2 * np.pi * freq * newt) + coes * np.sin(2 * np.pi * freq * newt)

newy-=40

print("inverse end")
plt.figure()
plt.plot(datalistx[:,0], datalistx[:,1], c='r', label='orginal')
print(datalistx[:,1])
print(newy)
plt.plot(newt, newy, c='b', label='fft')
plt.legend()
plt.show()
