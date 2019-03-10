from microbit import *
import random

random.seed()

display.scroll("Kop=A, Munt=B")

keuze = ""

while True:
    if button_a.is_pressed():
        keuze = "kop"
        droid.makeToast("TEST: A is ingedrukt!")
        break
 

    if button_b.is_pressed():
        keuze = "munt"
        droid.makeToast("TEST: b is ingedrukt!")
        break

    
x=(str(random.randint(1,2)))
if x==1:
    x="kop"
else:
    x="munt"



if keuze==x:
    display.scroll("Geraden")
else:
    display.scroll("Nope")
