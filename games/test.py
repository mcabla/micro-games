import android, time

droid = android.Android()

droid.makeToast("test")
time.sleep(5)

while 1:
	x = droid.getPackageVersionCode("com.mcabla.microbit.game")
	y = droid.button_a_was_pressed()
	txt = "app version is: " + str(x) + " button A was pressed: " + str(y)
	droid.makeToast(txt)
	time.sleep(5)
