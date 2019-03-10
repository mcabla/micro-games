import android, time

droid = android.Android()

droid.makeToast("test2")
time.sleep(5)

while 1:
	y = droid.button_a_was_pressed()
	txt = "Button A was pressed: " + str(y)
	droid.makeToast(txt)
	time.sleep(5)
