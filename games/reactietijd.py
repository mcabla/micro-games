#wie kortste reactietijd heeft wint
#wie hoogste score(= 1/reactietijd) heeft wint

from microbit import *
import time
import random

display.clear()
tijd=random.randint(3,9)        #random tijd tussen 4 en 9 seconden
tijd *= 1000                    #milliseconden van maken

gedrukt=False

time.sleep(100)
timer = 0
display.show("R")
time.sleep(100)
start = running_time()
reactietijd = 0
score = 0

while timer<tijd:               #wachttijd
  display.show(Image("00000:00000:90909:00000:00000"))
  button_a.was_pressed()
  button_b.was_pressed()
  timer= running_time() - start
  time.sleep(1)


while not gedrukt:              #snel drukken na wachttijd
  display.show(Image("99999:99999:99999:99999:99999"))
  if (button_a.was_pressed() or button_b.was_pressed()) and (running_time()-start)>=tijd:
    reactietijd = running_time() - start - tijd
    gedrukt = True
    display.scroll(reactietijd)         #tijd in milliseconden
    
score = 1/reactietijd
