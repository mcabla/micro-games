
from microbit import *
import time
import sys

bodem = Image("00000:"
              "00000:"
              "00000:"
              "00000:"
              "99999")

wegvallen_counter1 = 0
wegvallen_counter2 = 0
score = 0

def punten():
    global score
    if score == 0:    
      for p in range(0, 5):
          if display.get_pixel(p, 0) == 9:
             score += 1



def wegvallen1():
    global wegvallen_counter1

    for p in range(0, 5):

        if display.get_pixel(p, 3) == 0 and display.get_pixel(p, 2) == 9:
            display.set_pixel(p, 2, 0)
            wegvallen_counter1 += 1


def wegvallen2():
    global wegvallen_counter2

    for p in range(0, 5):

        if display.get_pixel(p, 2) == 0 and display.get_pixel(p, 1) == 9:
            display.set_pixel(p, 1, 0)
            wegvallen_counter2 += 1


def lijn1():
    wegvallen2()
    
    if button_b.is_pressed():
      
      exit()
      
      
    if not button_b.is_pressed():

        if wegvallen_counter1 == 0 and wegvallen_counter2 == 0:
            for t in range(0, 10000):
                stop = False

                for y in range(0, 2):
                    display.set_pixel(4 - y, 0, 9)
                    display.set_pixel(3 - y, 0, 9)
                    display.set_pixel(2 - y, 0, 9)

                    time.sleep(0.1)
                    if button_b.is_pressed():
                      
                      
                      stop = True
                      punten()
                      exit()



                      break

                    display.set_pixel(4 - y, 0, 0)

                if stop is True:
                  punten()
                  exit()
                    

                  break

                for z in range(0, 3):
                    display.set_pixel(2 + z, 0, 9)
                    display.set_pixel(1 + z, 0, 9)
                    display.set_pixel(0 + z, 0, 9)

                    if z != 2:
                        time.sleep(0.1)
                        if button_b.is_pressed():
                            stop = True
                            punten()
                            exit()

                            
                            break

                        display.set_pixel(0 + z, 0, 0)
                    if button_b.is_pressed():
                        stop = True
                        punten()
                        exit()
                        

                        break

                if stop is True:
                  punten()
                  exit()
                   

                  break
        elif wegvallen_counter1 == 2 and wegvallen_counter2 == 1 or wegvallen_counter1 == 1 and wegvallen_counter2 == 2:
          punten()
          exit()
        elif wegvallen_counter1 == 1 and wegvallen_counter2 == 0 or wegvallen_counter1 == 0 and wegvallen_counter2 == 1:
            for t in range(0, 10000):
                stop = False

                for y in range(0, 4):
                    display.set_pixel(4 - y, 0, 9)
                    display.set_pixel(3 - y, 0, 9)

                    time.sleep(0.1)
                    if button_b.was_pressed():
                        punten()
                        stop = True
                        exit()

                        
                        break

                    display.set_pixel(4 - y, 0, 0)

                if stop is True:
                  punten()
                  exit()
                    

                  break

                for z in range(0, 3):
                    display.set_pixel(2 + z, 0, 9)
                    display.set_pixel(1 + z, 0, 9)

                    if z == 0:
                        display.set_pixel(0, 0, 0)

                    if z != 2:
                        time.sleep(0.1)
                        display.set_pixel(0, 0, 0)

                        if button_b.is_pressed():
                            punten()
                            stop = True
                            exit()

                            
                            break
                        display.set_pixel(1 + z, 0, 0)

                    if button_b.is_pressed():
                        punten()
                        stop = True
                        exit()
                        

                        break
                display.set_pixel(0, 0, 0)

                if stop is True:
                  punten()
                  exit()
                    

                  break
                    
        elif wegvallen_counter1 == 0 and wegvallen_counter2 == 1 or wegvallen_counter1 == 1 and wegvallen_counter2 == 1 or wegvallen_counter1 == 2 and wegvallen_counter2 == 0:

                for t in range(0, 10000):
                    stop = False

                    for y in range(0, 5):
                        display.set_pixel(4 - y, 0, 9)

                        time.sleep(0.1)
                        if button_b.is_pressed():
                            punten()
                            stop = True
                            exit()



                            break

                        display.set_pixel(4 - y, 0, 0)

                    if stop is True:
                      punten()
                      exit()


                      break

                    for z in range(0, 4):
                        display.set_pixel(0 + z, 0, 9)

                        if z == 0:
                            display.set_pixel(1, 0, 9)
                            display.set_pixel(0, 0, 0)

                        if z != 4 and not z == 0:
                            time.sleep(0.1)

                            if button_b.is_pressed():
                                punten()
                                stop = True
                                exit()


                                break

                            display.set_pixel(0 + z, 0, 0)

                            if button_b.is_pressed():
                                punten()
                                stop = True
                                exit()
                                

                                break
                    display.set_pixel(0, 0, 0)

                    if stop is True:
                      punten()
                      exit()


                      break


        

def lijn2():
        wegvallen1()

        if not button_b.is_pressed():

            if wegvallen_counter1 == 0:

                for t in range(0, 10000):
                    stop = False

                    for y in range(0, 2):
                        display.set_pixel(4 - y, 1, 9)
                        display.set_pixel(3 - y, 1, 9)
                        display.set_pixel(2 - y, 1, 9)

                        time.sleep(0.08)
                        if button_b.was_pressed():
                            stop = True

                            lijn1()

                            break

                        display.set_pixel(4 - y, 1, 0)

                    if stop:
                        lijn1()

                        break

                    for z in range(0, 3):
                        display.set_pixel(2 + z, 1, 9)
                        display.set_pixel(1 + z, 1, 9)
                        display.set_pixel(0 + z, 1, 9)

                        if z != 2:
                            time.sleep(0.08)
                            if button_b.was_pressed():
                                stop = True

                                lijn1()
                                break

                            display.set_pixel(0 + z, 1, 0)
                        if button_b.was_pressed():
                            stop = True
                            lijn1()

                            break

                    if stop:
                        lijn1()

                        break

            elif wegvallen_counter1 == 1:

                for t in range(0, 10000):
                    stop = False

                    for y in range(0, 4):
                        display.set_pixel(4 - y, 1, 9)
                        display.set_pixel(3 - y, 1, 9)

                        time.sleep(0.08)
                        if button_b.was_pressed():
                            stop = True

                            lijn1()

                            break

                        display.set_pixel(4 - y, 1, 0)

                    if stop:
                        lijn1()

                        break

                    for z in range(0, 3):
                        display.set_pixel(2 + z, 1, 9)
                        display.set_pixel(1 + z, 1, 9)

                        if z == 0:
                            display.set_pixel(0, 1, 0)

                        if z != 2:
                            time.sleep(0.15)
                            display.set_pixel(0, 1, 0)

                            if button_b.was_pressed():
                                stop = True

                                lijn1()
                                break
                            display.set_pixel(1 + z, 1, 0)

                        if button_b.was_pressed():
                            stop = True
                            lijn1()

                            break
                    display.set_pixel(0, 1, 0)

                    if stop:
                        lijn1()

                        break

            elif wegvallen_counter1 == 2:

                for t in range(0, 10000):
                    stop = False

                    for y in range(0, 5):
                        display.set_pixel(4 - y, 1, 9)

                        time.sleep(0.08)
                        if button_b.was_pressed():
                            stop = True

                            lijn1()

                            break

                        display.set_pixel(4 - y, 1, 0)

                    if stop:
                        lijn1()

                        break

                    for z in range(0, 4):
                        display.set_pixel(0 + z, 1, 9)

                        if z == 0:
                            display.set_pixel(1, 1, 9)
                            display.set_pixel(0, 1, 0)

                        if z != 4 and not z == 0:
                            time.sleep(0.08)

                            if button_b.was_pressed():
                                stop = True

                                lijn1()
                                break
                            display.set_pixel(0 + z, 1, 0)

                        if button_b.was_pressed():
                            stop = True
                            lijn1()

                            break
                    display.set_pixel(0, 1, 0)

                    if stop:
                        lijn1()

                        break


def lijn3():
        if not button_b.is_pressed():

            for t in range(0, 10000):
                stop = False

                for y in range(0, 2):

                    display.set_pixel(4 - y, 2, 9)

                    display.set_pixel(3 - y, 2, 9)

                    display.set_pixel(2 - y, 2, 9)

                    time.sleep(0.15)
                    if button_b.was_pressed():
                        stop = True
                        lijn2()
                        break

                    display.set_pixel(4 - y, 2, 0)

                if stop:
                    lijn2()
                    break

                for z in range(0, 3):

                    display.set_pixel(2 + z, 2, 9)

                    display.set_pixel(1 + z, 2, 9)

                    display.set_pixel(0 + z, 2, 9)
                    if z != 2:
                        time.sleep(0.15)
                        if button_b.was_pressed():
                            stop = True
                            lijn2()
                            break

                        display.set_pixel(0 + z, 2, 0)
                    if button_b.was_pressed():
                        stop = True
                        lijn2()
                        break

                if stop:
                    lijn2()
                    break


def lijn4():
        # ////////////////////////////////////
        if not button_b.is_pressed():

            for t in range(0, 10000):
                stop = False

                for y in range(0, 2):
                    display.set_pixel(4 - y, 3, 9)
                    display.set_pixel(3 - y, 3, 9)
                    display.set_pixel(2 - y, 3, 9)

                    time.sleep(0.15)
                    if button_b.was_pressed():
                        stop = True
                        lijn3()
                        break

                    display.set_pixel(4 - y, 3, 0)

                if stop:
                    lijn3()
                    break

                for z in range(0, 3):
                    display.set_pixel(2 + z, 3, 9)
                    display.set_pixel(1 + z, 3, 9)
                    display.set_pixel(0 + z, 3, 9)

                    if z != 2:
                        time.sleep(0.15)
                        if button_b.was_pressed():
                            stop = True
                            lijn3()

                            break

                        display.set_pixel(0 + z, 3, 0)
                    if button_b.was_pressed():
                        stop = True
                        lijn3()

                        break

                if stop:
                    lijn3()

                    break

        # ////////////////////////////////////


display.show(bodem)

while True:
    try:
      if button_a.is_pressed():
        lijn4()
        
    except:
      
      if score == 1:
        display.show(str(1))
      elif score == 0:
        display.show(str(0))
      elif score == 2:
        display.show(str(3))
      elif score == 3:
        display.show(str(6))
      
      







