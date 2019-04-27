from microbit import *
import random
import time


score = 0

run = True
while run:
    getal = random.randrange(0,9) # willekeurig getal dat op het scherm komt
    willekeurig = random.randrange(0,9) # het getal naar waar gegokt moet worden
    getal_str = str(getal)
    display.show(getal_str)
    while True:

        if button_a.was_pressed(): # Knop A: het te zoeken getal is LAGER dan het getal dat op het scherm staat
            if int(willekeurig) >= int(getal):
                score += 1
                display.scroll("JUIST!")
                #time.sleep(3)        
            else:
                display.scroll("FOUT!")
                #time.sleep(3)
                run = False
            break
        
        
        elif button_b.was_pressed(): # Knop B: het te zoeken getal is HOGER dan het getal dat op het scherm staat
            if int(willekeurig) <= int(getal):
                score += 1
                display.scroll("JUIST!")
                #time.sleep(3)
            else:
                display.scroll("FOUT!")
                #time.sleep(3)
                run = False
            break
        
        time.sleep(0.02)

display.scroll("SCORE: " + str(score))
time.sleep(4)
