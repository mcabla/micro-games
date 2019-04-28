from microbit import *
import random
import time


score = 0

for i in range(0,4):
    time.sleep(1)
    getal = random.randrange(0,9)
    # display.scroll("Kies A voor Lager, B voor Hoger")

    willekeurig = random.randrange(0,9)
	willekeurig_str = str(willekeurig) #Wegens een beperking in de python implementatie op Android, moeten we helaas onze strings maken buiten de parameters van de microbit functies
    display.show(willekeurig_str)
    
    

    while True:

		if button_a.was_pressed()
			if willekeurig > getal:
				score += 1
				str = 'JUIST! ' + str(getal)
				display.scroll(str)
				time.sleep(3) #Wegens een beperking in de python implementatie op Android, moeten we handmatig een sleep toevoegen achter een scroll
				break
			else:
				str = 'FOUT! ' + str(getal)
				display.scroll(str)
				time.sleep(3)
				break
        
        
		if button_b.was_pressed()
			if willekeurig < getal:
				score += 1
				str = 'JUIST! ' + str(getal)
				display.scroll(str)
				time.sleep(3)
				break
			else:
				str = 'FOUT! ' + str(getal)
				display.scroll(str)
				time.sleep(3)
				break
		
		time.sleep(0.05) #We zetten een niet merkbare vertraging op de lus om te voorkomen dat er niet te veel requests naar de microbit gestuurd worden.

str = 'SCORE: ' + str(score)
display.scroll(str)
time.sleep(4)
