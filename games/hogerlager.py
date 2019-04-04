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

      if button_a.is_pressed() and int(willekeurig) > int(getal):

        display.clear()
        score += 1
        display.scroll("JUIST!")
        display.scroll(int(getal))
        break
        
      elif button_a.is_pressed() and int(willekeurig) < int(getal) :
        display.scroll("FOUT!")
        display.scroll(int(getal))
        break
        
        
      if button_b.is_pressed() and int(willekeurig) < int(getal):

        display.clear()
        score += 1
        display.scroll("JUIST!")
        display.scroll(int(getal))
        break

      elif button_b.is_pressed() and int(willekeurig) > int(getal):

        display.clear()
        display.scroll("FOUT!")
        display.scroll(int(getal))
        break

display.scroll("SCORE:")
display.show(str(score))
               
              
             


  

             


  
 
