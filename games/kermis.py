from microbit import *
import time
#maak een bodem en de speler zijn eerste bewegende lijn.
bodem = Image("00000:"
              "00000:"
              "00000:"
              "99900:"
              "99999")
#maak de globale score, lengte, plaats en verplaatsing aan
score = 0
lengte = 3
huidige_y = 3 #y coördinaat van de linkse pixel van de huidige bewegende lijn
huidige_x = 0 #x coördinaat van de linkse pixel van de huidige bewegende lijn
dx = 1 #verplaatsing (1 = naar rechts, -1 = naar links)

#verplaats de lijn naar links
def links(x,y,lengte):
  display.set_pixel(x + lengte-1, y,0) #linker pixel uit
  display.set_pixel(x-1,y,9) #rechter pixel aan
  
#verplaats de lijn naar rechts
def rechts(x,y,lengte):
  display.set_pixel(x,y,0) #rechter pixel uit
  display.set_pixel(x + lengte, y,9) #linker pixel aan
  
  
def verplaats(): 
  global lengte
  global huidige_y
  global huidige_x
  global dx
  if dx == 1: # als de verplaatsing naar rechts is
    if huidige_x + lengte == 5: # en de lijn zich al volledig rechts bevindt
        links(huidige_x,huidige_y,lengte) #ga dan naar links
        dx = -1 # en stel de verplaatsing in naar links
    else: #en er is nog plaats aan de rechterkant
        rechts(huidige_x,huidige_y,lengte) #ga naar rechts
  else:  # als de verplaatsing naar links is
    if huidige_x == 0: # en de lijn zich al volledig links bevindt
        rechts(huidige_x,huidige_y,lengte) #ga dan naar rechts
        dx = +1 # en stel de verplaatsing in naar rechts
    else: #en er is nog plaats aan de linkerkant
        links(huidige_x,huidige_y,lengte) #ga dan naar links
  huidige_x += dx #tel het nieuwe x coördinaat in
  
  
def krijgLengte(x,y):
  global lengte
  l = 0 #tijdelijke lengte
  for j in range(0,lengte): #kijk voor elke pixel van de bewegende lijn of de pixel er onder brandt
    if display.get_pixel(x + j,y+1) != 0:
        l += 1 #zo ja verleng de tijdelijke lengte met 1
    else:
      display.set_pixel(x+j,y,0) #zo nee zet de pixel uit
  lengte = l #stel de tijdelijke lengte in als de nieuwe lengte
  
#maak een nieuwe lijn met de gegeven lengte vanaf x = 0 op de gegeven hoogte y 
def zetNieuweLijn(y,lengte):
  for j in range(0,lengte):
    display.set_pixel(j,y,9)
  

display.show(bodem)
i = 0 #+1 bij elke keer dat de volgende lus uitgevoerd wordt.
k = 30 #snelheidsregelaar van de bewegende lijn
mag_plaatsen = False #dit zorgt ervoor dat de speler niet direct een lijn kan plaatsen


while 1:
  if i >= k: # de volgende codes worden maar om de k keren uitevoerd.
      verplaats() #verplaats de lijn met 1 pixel
      i = 0
      if not mag_plaatsen: #vanaf nu mag de speler een lijn plaatsen
          button_a.was_pressed() #we resetten deze booleans wel eerst
          button_b.was_pressed() #zo kan een speler minder makkelijk valsspelen
          mag_plaatsen = True
  time.sleep(0.01) #we werken met 1 sleep timer, de lus loopt dus om de 0.01 seconden
  if i % 2 == 0 and (button_a.was_pressed() or button_b.was_pressed()) and mag_plaatsen: #worden pas om de twee lussen uitgevoerd
    krijgLengte(huidige_x,huidige_y) #stel de nieuwe lengte in en verwijder de hangende pixels
    if lengte == 0:
      break #je hebt geen pixels meer over
    huidige_y -= 1
    huidige_x = 0
    if huidige_y == 2:
      k = 10 #snelheid voor lijn 2
    elif huidige_y == 1: 
      k = 5 #snelheid voor lijn 1
    elif huidige_y == 0:
      k = 4 #snelheid voor lijn 0, als deze nog kleiner is, kan het zijn dat de micro:bit niet mee kan met de snelheid van het scriptje
    else:
      break #stop, we zijn bovenaan
    zetNieuweLijn(huidige_y,lengte)
    mag_plaatsen = False
    i == 0
  i += 1
  
score = lengte
score_str = str(score) #een moeilijke fout in de app zorgt ervoor dat we eerst een string moeten maken vooraleer we deze showen
display.show(score_str)
