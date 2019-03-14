from microbit import *
import random, time

while True:
    if button_a.is_pressed():
        blad = Image("00000:"
             "09990:"
             "09990:"
             "09990:"
             "09990:")
        display.show(blad)

 

    if button_b.is_pressed():
        display.set_pixel(1,1,0)
