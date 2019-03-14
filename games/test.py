from microbit import *
import random, time

random.seed()

display.scroll("Kop=A, Munt=B")
keuze = ""

while True:
    if button_a.is_pressed():
        display.set_pixel(1,1,9)
        display.set_pixel(1,2,0)
 

    if button_b.is_pressed():
        display.set_pixel(1,2,9)
        display.set_pixel(1,1,0)
