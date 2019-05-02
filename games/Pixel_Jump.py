from microbit import*
import time
starttijd = running_time()

#sprite

hoogteP1 = 4
hoogteP2 = 3

P1 = 9
P2 = 9

def start():
    global hoogteP1
    global hoogteP2
    global P1
    global P2
    
    display.set_pixel(0, hoogteP1, P1)
    display.set_pixel(0, hoogteP2, P2)
#-------------------------------------
 
  
#springen
def spring():
    global hoogteP1
    global hoogteP2
    global P1
    global P2
  
    P1 = 0
    P2 = 0
    start()
    
    P1 = 9
    P2 = 9
    hoogteP1 -= 2
    hoogteP2 -= 2
    start()
 #------------------------------------

#vallen
def val():
    global hoogteP1
    global hoogteP2
    global P1
    global P2
  
  
    P1 = 0
    P2 = 0
    start()
   
    P1 = 9
    P2 = 9
    hoogteP1 += 2
    hoogteP2 += 2
    start()
#------------------------------------
    

#obstakel
ObstakelX = 4

O = 9

def obstakel():
    global ObstakelX
    global O
    
    display.set_pixel(ObstakelX, 4, O)
#------------------------------------

 
def bewegend_obstakel(): 
    global ObstakelX
    global O
  
    O = 0
    obstakel()
    if ObstakelX == 0 and hoogteP1 == 4:
        display.set_pixel(0, 4, 9)
    
    O = 9
    ObstakelX -= 1
    if ObstakelX == -1:
        ObstakelX = 4
    obstakel()    

    
#------------------------------------
  
aan_het_springen = False
k = 0

def speler():
    global aan_het_springen
    global k
# k is RE met tijd in de lucht  
    if aan_het_springen == True:
        if k == 20:
            val()
            aan_het_springen = False
        else:
            k += 1
          
    
    elif button_a.was_pressed() or button_b.was_pressed():
        spring()
        aan_het_springen = True
        k = 0
  
#------------------------------------  
  

#spel
i = 0
j = 0
m = 0
obstakel()
start()
in_leven = True
gewenning = True
versnelling = 0

#------------------------------------
# aftellen
cijfer3 = Image("09990:"
                "90009:"
                "00990:"
                "90009:"
                "09990:")

display.show(cijfer3)
time.sleep(1)

cijfer2 = Image("09990:"
                "90009:"
                "00090:"
                "00900:"
                "99999:")

display.show(cijfer2)
time.sleep(1)

cijfer1 = Image("00900:"
                "09900:"
                "00900:"
                "00900:"
                "09990:")

display.show(cijfer1)
time.sleep(1)

display.clear()

start()
button_a.was_pressed()
button_b.was_pressed()


while gewenning:
    m += 1
    speler()
    time.sleep(0.02)
    if m == 200:
        gewenning = False

obstakel()
while in_leven:      
    speler()
    if i ==(10):
# hier is j RE met tempo versnellen        
        if j % 20 == 0 and j < 500:
            versnelling += 1
        
          
        i = versnelling
        
        bewegend_obstakel()
    
    if ObstakelX == 0 and hoogteP1 == 4:
        in_leven = False
        
    else:
        i += 1
        j += 1
    time.sleep(0.02)
    
display.scroll("Game over")
score = running_time()
time.sleep(5)

