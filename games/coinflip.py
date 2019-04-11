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

    
x= random.randint(0,2)
if x==1:
    x="kop"
else:
    x="munt"


score = 0
if keuze==x:
    display.scroll("Geraden")
    score = 1
else:
    display.scroll("Nope")
