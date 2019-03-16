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


#random gegenereerde keuze blad, steen, schaar
keuze1 =(random.randint(0,3))
keuze2 = [blad, steen, schaar]


if keuze1 == 1:
  keuze1 = "blad" #0
elif keuze1 == 2:
  keuze1 = "steen" #1
elif keuze1 == 3:
  keuze1 = "schaar" #2
  
#keuze speler
display.scroll("veranderen = A, selecteren = B")

huidige = 0
display.show(blad)
while 1:
    if button_a.get_presses()!= 0:
        huidige +=1
        if huidige > 2:
            huidige = 0
      
        display.show(keuze2[huidige])

            
    elif button_b.get_presses() != 0:
        break
    
    time.sleep(0.2)
      
    
#bepalen winnaar
if keuze1=="blad":
    if huidige == 0:
        display.scroll("gelijkspel")
    elif huidige == 1:
        display.scroll("verloren")
    elif huidige == 2:
        display.scroll("gewonnen")

elif keuze1 == "steen":
    if huidige == 0:
       display.scroll("gewonnen")
    elif huidige == 1:
        display.scroll("gelijkspel")
    elif huidige == 2:
        display.scroll("verloren")
elif keuze1 == "schaar":
    if huidige == 0:
        display.scroll("verloren")
    elif huidige == 1:
        display.scroll("gewonnen")
    elif huidige == 2:
        display.scroll("gelijkspel")
