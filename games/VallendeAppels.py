#'appels' vallen steeds sneller naar beneden
# variabele score is gevangen appels
# aftellen in het begin

from microbit import *
import time
import random


bezig = True
appel_aanwezig = False
score = 0 
tics = 11   #variabele die toelaat om de appels sneller te laten vallen



def linksrechts():        #functie om met knoppen A en B naar links en rechts te bewegen
  global x
  if button_a.was_pressed():
    if x>0:
      display.set_pixel(x,4,0)
      x-=1
      display.set_pixel(x,4,9)
  elif button_b.was_pressed():
    if x<4:
      display.set_pixel(x,4,0)
      x+=1
      display.set_pixel(x,4,9)
  time.sleep(0.05)
    

def nieuwe_appel():   #genereren van een nieuwe appel
  global appel_aanwezig
  global xa
  global ya
  appel_aanwezig=True
  ya=0
  xa = random.randint(0,4)
  display.set_pixel(xa,ya,6)
  
  
def appel_valt():
  global appel_aanwezig
  global xa
  global ya
  i=1
  while ya<4:
    linksrechts()
    if i%tics==0:
      if ya<3:
        linksrechts()
        display.set_pixel(xa,ya,0)
        ya+=1
        display.set_pixel(xa,ya,6)
      else:  
        linksrechts()
        appel_aanwezig=False
        break
    i+=1
  display.set_pixel(xa,ya,0)
  appel_aanwezig=False

def check():
  global score
  global x
  global bezig 
  
  if x==xa:
    score+=1
    pass
  else:
    bezig = False
  


display.show('3')   #aftellen voor spel
time.sleep(1)
display.show('2')
time.sleep(1)
display.show('1')
time.sleep(1)
display.show(Image("00000:00000:00000:00000:00000"))
x=2
display.set_pixel(x,4,9)     #pixel onderaan midden zetten

bezigint=0

while bezig:
  linksrechts()
  if not appel_aanwezig:
    nieuwe_appel()
  appel_valt()
  check()
  if tics>1 and bezigint%2==0:    #1 op 2 bezigint de snelheid verhogen
    tics-=1
  bezigint+=1

display.scroll(score)       #score laten zien

  
