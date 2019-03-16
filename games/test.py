from microbit import *
import random
import time

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

i = 0
while True:
    if button_a.was_pressed():
        i -= 1
        if i == -1:
            i = 3
        display.show(keuze2[i])
 
    elif button_b.was_pressed():
        i += 1
        if i == 3:
            i = 0
        display.show(keuze2[i])
    time.sleep(0.2)
