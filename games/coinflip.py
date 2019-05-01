from microbit import *
import random
import time

random.seed()

display.scroll("Kop=A, Munt=B")
keuze = ""

while True:
    if button_a.is_pressed():
        keuze = "kop"
        break
 

    if button_b.is_pressed():
        keuze = "munt"
        break

#worp
fase1 = Image("00000:"
              "00000:"
              "00000:"
              "00000:"
              "09990:")
display.show(fase1)
time.sleep(0.3)
 
fase2 = Image("00000:"
              "00090:"
              "00900:"
              "09000:"
              "00000:")
display.show(fase2)
time.sleep(0.3)

fase3 = Image("00900:"
              "00900:"
              "00900:"
              "00000:"
              "00000:")
display.show(fase3)
time.sleep(0.3)

fase4 = Image("00000:"
              "09000:"
              "00900:"
              "00090:"
              "00000:")
display.show(fase4)
time.sleep(0.3)
display.show(fase1)
time.sleep(0.3)

#---    
x= random.randint(0,1)
if x==1:
    x="kop"
else:
    x="munt"


score = 0
if keuze==x:
    display.scroll("Geraden")
    time.sleep(3)
    score = 1
else:
    display.scroll("Fout")
    time.sleep(2)
