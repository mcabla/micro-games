import android, time

droid = android.Android()

while 1:
	droid.makeToast("test")
	time.sleep(5)
	x = droid.getPackageVersionCode("com.mcabla.microbit.game")
	y = droid.button_a_was_pressed()
	txt = "app version is: " + str(x) + " button A was pressed: " + str(y)
	droid.makeToast(txt)
	time.sleep(5)
