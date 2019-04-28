from microbit import *
import random
import time


score = 0
random.seed()

for i in range(0,4):
	time.sleep(1)
	getal = random.randrange(0,9)
	# display.scroll("Kies A voor Lager, B voor Hoger")
	willekeurig = random.randrange(1,8)
	willekeurig_str = str(willekeurig) #Wegens een beperking in de python implementatie op Android, moeten we helaas onze strings maken buiten de parameters van de microbit functies
	display.show(willekeurig_str)
	button_a.was_pressed()
	button_b.was_pressed()
	
	while True:
		time.sleep(0.2) #We zetten een niet merkbare vertraging op de lus om te voorkomen dat er niet te veel requests naar de microbit gestuurd worden.
		if button_a.was_pressed():
			if willekeurig > getal:
				score += 1
				display.scroll("JUIST! " + str(getal))
				time.sleep(6) #Wegens een beperking in de python implementatie op Android, moeten we handmatig een sleep toevoegen achter een scroll
				break
			else:
				display.scroll("FOUT! " + str(getal))
				time.sleep(6)
				break
		elif button_b.was_pressed():
			if willekeurig < getal:
				score += 1
				display.scroll("JUIST! " + str(getal))
				time.sleep(6)
				break
			else:
				display.scroll("FOUT! " + str(getal))
				time.sleep(6)
				break
display.scroll("SCORE=" + str(score))
time.sleep(4)
