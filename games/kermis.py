from microbit import *
import time

bodem = Image("00000:"
              "00000:"
              "00000:"
              "99900:"
              "99999")
score = 0
lengte = 3
huidige_y = 3
huidige_x = 0
dx = 1

def links(x,y,lengte):
  display.set_pixel(x + lengte-1, y,0)
  display.set_pixel(x-1,y,9)
  
  
def rechts(x,y,lengte):
  display.set_pixel(x,y,0)
  display.set_pixel(x + lengte, y,9)
  
  
def verplaats(): 
  global lengte
  global huidige_y
  global huidige_x
  global dx
  if dx == 1:
    if huidige_x + lengte == 5:
        links(huidige_x,huidige_y,lengte)
        dx = -1
    else:
        rechts(huidige_x,huidige_y,lengte)
  else:  
    if huidige_x == 0:
        rechts(huidige_x,huidige_y,lengte)
        dx = +1
    else:
        links(huidige_x,huidige_y,lengte)
  huidige_x += dx
  
  
def krijgLengte(x,y):
  global lengte
  l = 0
  for j in range(0,lengte):
    if display.get_pixel(x + j,y+1) != 0:
        l += 1
    else:
      display.set_pixel(x+j,y,0)
  lengte = l
  
  
def zetNieuweLijn(y,lengte):
  for j in range(0,lengte):
    display.set_pixel(j,y,9)
  

display.show(bodem)
i = 0
k = 30
mag_plaatsen = True


while 1:
  if i >= k:
      verplaats()
      i = 0
      if not mag_plaatsen:
          button_a.was_pressed()
          button_b.was_pressed()
          mag_plaatsen = True
  if i % 2 == 0 and (button_a.was_pressed() or button_b.was_pressed()) and mag_plaatsen:
    krijgLengte(huidige_x,huidige_y)
    if lengte == 0:
      break
    huidige_y -= 1
    huidige_x = 0
    if huidige_y == 2:
      k = 10
    elif huidige_y == 1:
      k = 5
    elif huidige_y == 0:
      k = 4
    else:
      break
    zetNieuweLijn(huidige_y,lengte)
    mag_plaatsen = False
    i == 0
  i += 1
  time.sleep(0.01)


score = lengte
score_str = str(score)
display.show(score_str)
