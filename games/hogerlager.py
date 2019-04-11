from microbit import *
import random
import time


score = 0

run = True
while run:
    getal = random.randrange(0,9)
    willekeurig = random.randrange(0,9)
    getal_str = str(getal)
    display.show(getal_str)
    while True:

        if button_a.was_pressed():
            if int(willekeurig) >= int(getal):
                score += 1
                display.scroll("JUIST!")
                #time.sleep(3)        
            else:
                display.scroll("FOUT!")
                #time.sleep(3)
                run = False
            break
        
        
        elif button_b.was_pressed():
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
