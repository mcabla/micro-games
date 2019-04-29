#wie kortste reactietijd heeft wint
#wie hoogste score(= 1/reactietijd) heeft wint
#valsspelers krijgen score 0

from microbit import *
import time
import random

display.clear()
tijd=random.randint(3,9)        #random tijd tussen 4 en 9 seconden
tijd *= 1000                    #milliseconden van maken

gedrukt=False

time.sleep(0.100)
timer = 0
display.show("R")
time.sleep(1)
start = running_time()
reactietijd = 0
score = 0

button_a.was_pressed()
button_b.was_pressed()

while timer<tijd:               #wachttijd
  display.show(Image("00000:00000:90909:00000:00000"))
  timer = running_time() - start
  time.sleep(1)

display.show(Image("99999:99999:99999:99999:99999"))

start2= running_time()  #start2 telt vanaf het scherm rood is

while not gedrukt:              #snel drukken na wachttijd
  if (button_a.was_pressed() or button_b.was_pressed()):
    gedrukt=True
    if (running_time()-start2)>=300:     
      reactietijd = int(running_time() - start2)
      display.scroll(reactietijd)         #tijd in milliseconden
      score = (1/reactietijd)*1000
    else:
      score = 0
      display.scroll("Valse start")
      time.sleep(3)
  time.sleep(0.01)
