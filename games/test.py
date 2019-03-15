from microbit import *
import random
import time

while True:
    if button_a.was_pressed():
        blad = Image("00000:"
             "09990:"
             "09990:"
             "09990:"
             "09990:")
        display.show(blad)
 
    if button_b.was_pressed():
        display.set_pixel(1,1,0)
    time.sleep(30)
