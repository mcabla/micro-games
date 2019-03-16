from microbit import *
import random
import time

random.seed()

#afbeeldingen
blad = Image("00000:"
             "09990:"
             "09990:"
             "09990:"
             "09990:")


steen = Image("00000:"
              "00000:"
              "09990:"
              "99999:"
              "99999:")


schaar = Image("99009:"
               "99090:"
               "00900:"
               "99090:"
               "99009:")

keuze2 = [blad, steen, schaar]

display.show(blad)

i = 0
while True:
    if button_a.was_pressed():
        i += 1
        if i == 3:
            i = 0
        display.show(keuze2[i])
 
    elif button_b.was_pressed():
        break
    time.sleep(0.2)
    
i2 = random.randint(0,2)

score = 0
if i == i2:
  score = 1
  display.scroll('gelijkspel')
else:
    if i2==0:
        if i == 2:
            score = 2
            display.scroll("gewonnen")
        elif i == 1:
            display.scroll("verloren")
    elif i2 == 1:
        if i == 0:
            score = 2
            display.scroll("gewonnen")
        elif i == 2:
            display.scroll("verloren")
    elif keuze1 == 2:
        if huidige == 0:
            display.scroll("verloren")
        elif huidige == 1:
            score = 2
            display.scroll("gewonnen")
