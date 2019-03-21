#op knopjes drukken en telleln


from microbit import *
import time

aantal=0
tijd = True
start = running_time()+1000000
eind = 0
verschil = 0

while tijd:
  sleep(50)
  if button_a.was_pressed():
    start = min(start, running_time())
    aantal +=1
  if aantal < 10:
    display.show('0')               #0 tot 10 laat 0 zien
  else:
    display.show(str(aantal)[0])    # per 10 toont hij 1,2,3,...
  if aantal >=25:
    eind = running_time()
    verschil = (eind - start)/1000
    display.scroll(str(verschil))
    sleep(1000)
    break
    
# record 3,644 tessa
# record 3,37 lennert
