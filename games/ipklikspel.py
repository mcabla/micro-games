#op knopjes drukken en tellen
#winnaar heeft kortste tijd (variabele verschil)

from microbit import *
import time
display.clear()

aantal=0
tijd = True
start = running_time()+1000000
eind = 0
verschil = 0

while tijd:
  sleep(10)
  if button_a.was_pressed():
    start = min(start, running_time())
    aantal +=1
    if aantal >= 10:  
      if aantal >=50:
        eind = running_time()
        verschil = (eind - start)/1000
        display.scroll(str(verschil))
        sleep(1000)
        break
      stra = str(aantal)
      x=int(stra[1])/2
      y=int(stra[0])
      if aantal % 2 == 0:
        display.set_pixel(x, y, 9)
    elif aantal < 10:
      if aantal % 2 == 0:
        display.set_pixel(aantal/2, 0, 9)
  
  #if aantal < 10:
  #  display.show('0')               #0 tot 10 laat 0 zien
  #else:
  #  display.show(str(aantal)[0])    # per 10 toont hij 1,2,3,...
  
    
# record 3,5 tessa
# record 3,37 lennert   (2,39 als hij cheat)

