import android, time

droid = android.Android()

while 1:
	droid.makeToast("test")
	time.sleep(5)
	x = droid.getPackageVersionCode("com.mcabla.microbit.game")
	txt = "app version is: " + str(x)
	droid.makeToast(txt)
	time.sleep(5)
