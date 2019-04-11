from microbit import *
import random
import time


score = 0

for i in range(0,4):
    time.sleep(1)
    getal = random.randrange(0,9)
    # display.scroll("Kies A voor Lager, B voor Hoger")

    willekeurig = random.randrange(0,9)
    display.show(str(willekeurig))
    
    

    while True:

      if button_a.was_pressed() and int(willekeurig) > int(getal):

        display.clear()
        score += 1
        display.scroll("JUIST!")
        time.sleep(3)
        display.scroll(str(getal))
        break
        
      elif button_a.was_pressed() and int(willekeurig) < int(getal) :
        display.scroll("FOUT!")
        time.sleep(3)
        display.scroll(str(getal))
        break
        
        
      if button_b.was_pressed() and int(willekeurig) < int(getal):

        display.clear()
        score += 1
        display.scroll("JUIST!")
        time.sleep(3)
        display.scroll(str(getal))
        break

      elif button_b.was_pressed() and int(willekeurig) > int(getal):

        display.clear()
        display.scroll("FOUT!")
        time.sleep(3)
        display.scroll(str(getal))
        break
        
      time.sleep(0.02)

display.scroll("SCORE: " + str(score))
              
              
             


  

             


  
 
