#op knopjes drukken en tellen
#winnaar heeft kortste tijd (variabele verschil)
#winnaar heeft hoogste score (variabele score = 1/verschil)

from microbit import *
import time
display.clear()
img1 =  Image("99999:"
              "95559:"
              "95059:"
              "95559:"
              "99999")

img2 =  Image("55555:"
              "50005:"
              "50905:"
              "50005:"
              "55555")

img3 =  Image("00000:"
              "09990:"
              "09590:"
              "09990:"
              "00000")
imgs = [img1,img2,img3]
aantal=0
tijd = True
start = running_time()+1000000
eind = 0
verschil = 0

while tijd:
  time.sleep(10)
  if button_a.was_pressed():
    start = min(start, running_time())
    aantal +=1
    if aantal >= 10:  
      if aantal >=50:
        eind = running_time()
        verschil = (eind - start)/1000
        display.show(imgs, delay=200)
        display.show(imgs, delay=200)
        display.scroll(str(verschil))
        time.sleep(1000)
        score = 1/verschil
        break
      stra = str(aantal)
      y=int(stra[0])
      if stra[1]=='0':
        x=5
        y-=1
      else:
        x=int(stra[1])/2
      
      if aantal % 2 == 0:
        display.set_pixel(x-1, y, 9)
    elif aantal < 10:
      if aantal % 2 == 0:
        display.set_pixel(aantal/2-1, 0, 9)
