# IoT Music Visualization

Project for Visualization of Different Music Pitches in a particular range to lights in a linear HSV pallete .

## How it works
1. Records Audio Samples in an Andorid Phone at sampling frequency 22050 Hz
2. Detects most probalble pitch of the sound sample 
3. Maps the frequency into HSV values and sends to ESP32 Device
4. Device converts the HSV values to RGBW values and relays to LED bulb using PWM

IoT music visualization using ESP32 for mapping HSV values from Android App to RGBW lights .

## Build It Yourself

### App
Import andorid studio project in Andorid Studio .
To send to IoT Device running a Web server change IP in :-
```code
float h=(pitch/400)*360;
        JSONObject jsonObject= null;
        try {
            jsonObject = new JSONObject();
            if(h<10)
                return;
            if(h>360)
                h=360;
            jsonObject.put("h",h);
            jsonObject.put("s",1);
            jsonObject.put("v",1);
            Log.e("API REQ",jsonObject.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        reqInProgress=true;

        AndroidNetworking.post("http://192.168.4.1/rpc/color").addJSONObjectBody(jsonObject).build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                   //   tm.cancel();
                        reqInProgress=false;
                        Log.e("API",response);
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });
```



## Demo VIDEO CLICK TO OPEN
[![IoT Music Visualization](https://img.youtube.com/vi/q-aPQKLFCvA/0.jpg)](https://www.youtube.com/watch?v=q-aPQKLFCvA "Video : IoT Music Visualization")
