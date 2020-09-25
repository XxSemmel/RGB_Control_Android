#include <Arduino.h>

#include <FastLED.h>

// Which pin on the Arduino is connected to the NeoPixels?
// On a Trinket or Gemma we suggest changing this to 1:
#define LED_PIN 18
#define COLOR_ORDER GRB
#define CHIPSET WS2811

// How many NeoPixels are attached to the Arduino?
//#define LED_COUNT 105

#include <Preferences.h>

#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>

int LED_COUNT;
CRGB *leds;
// Declare our NeoPixel strip object:

// See the following for generating UUIDs:
// https://www.uuidgenerator.net/

#define SERVICE_UUID "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define SETTINGS_SERVICE_UUID "cfa34f22-6f5a-4587-95af-bc4cc057610b"
#define RGB_CHARACTERISTIC_UUID "0bca6a30-1418-4fd9-a7d2-950beb793e86"
#define CHANGE_DEVICENAME_UUID "3aa67643-5d02-4278-9db0-a95993c011d7"
#define RESTART_UUID "8b9787e5-8192-4104-a87e-a4fa80c304e6"
#define NEOPIXEL_NUMBER_UUID "99cf3c7b-0f41-4adc-ba90-0b5c6242af06"
#define EFFECT_UUID "8e341dc8-2907-49af-9e34-9da998eec5fc"

class MyCallbacks : public BLECharacteristicCallbacks
{
  void onWrite(BLECharacteristic *rgbCharacteristic)
  { //if value changed
    std::string value = rgbCharacteristic->getValue();

    if (value.length() > 0)
    { //if value bigger 0

      String rgb;
      for (int i = 0; i < value.length(); i++)
      {
        rgb = rgb + value[i]; //write in values in rgb
      }

      //Split String
      String rgb_r = GetValue(rgb, ' ', 0);
      String rgb_g = GetValue(rgb, ' ', 1);
      String rgb_b = GetValue(rgb, ' ', 2);

      //Set pixel colors
      for (int i = 0; i < LED_COUNT; i++)
      {
        // strip.setPixelColor(i, strip.Color(rgb_r.toInt(),rgb_g.toInt(),rgb_b.toInt()));
        leds[i] = CRGB(rgb_r.toInt(), rgb_g.toInt(), rgb_b.toInt());
      }

      FastLED.show(); // display this frame
    }
  }

  //Function for splitting string
  String GetValue(String data, char separator, int index)
  {
    int found = 0;
    int strIndex[] = {0, -1};
    int maxIndex = data.length() - 1;

    for (int i = 0; i <= maxIndex && found <= index; i++)
    {
      if (data.charAt(i) == separator || i == maxIndex)
      {
        found++;
        strIndex[0] = strIndex[1] + 1;
        strIndex[1] = (i == maxIndex) ? i + 1 : i;
      }
    }

    return found > index ? data.substring(strIndex[0], strIndex[1]) : "";
  }
};


class NeoPixelChangeCallback : public BLECharacteristicCallbacks
{
  Preferences pref;
  void onWrite(BLECharacteristic *changeNeoPixelNumber)
  { //if value changed
    std::string value = changeNeoPixelNumber->getValue();

    pref.begin("RGB-Control", false);

    if (value.length() > 0)
    { //if value bigger 0

      String number;

      for (int i = 0; i < value.length(); i++)
      {
        number = number + value[i];
      }

      pref.putString("LEDcount", number);
      pref.end();
    }
  }
};

class DeviceNameChange : public BLECharacteristicCallbacks
{
  Preferences pref;

  void onWrite(BLECharacteristic *changeDeviceName)
  { //if value changed
    std::string value = changeDeviceName->getValue();

    pref.begin("RGB-Control", false);

    if (value.length() > 0)
    { //if value bigger 0

      String newname;
      for (int i = 0; i < value.length(); i++)
      {
        newname = newname + value[i];
      }

      pref.putString("Name", newname);
      pref.end();
    }
  }
};

class RestartCallback : public BLECharacteristicCallbacks
{
  void onWrite(BLECharacteristic *restartDevice)
  { //if value changed
    std::string value = restartDevice->getValue();

    if (value.length() > 0)
    { //if value bigger 0

      ESP.restart();
    }
  }
};

class EffectCallback : public BLECharacteristicCallbacks
{

  TaskHandle_t task1Handle;

  bool isEffectrun = false;
  void onWrite(BLECharacteristic *effectCharacteristic)
  { //if value changed
    std::string value = effectCharacteristic->getValue();

    if (value.length() > 0)
    { //if value bigger 0

      String effectname;
      Preferences pref;
      pref.begin("RGB-Control", false);
      for (int i = 0; i < value.length(); i++)
      {
        effectname = effectname + value[i];
      }
      StopEffect();


      if (effectname == "Rainbow")
      {

        pref.putString("Effect", "rainbow");

        xTaskCreate(this->Task1, "EffectThread", 10000, this, 5, &task1Handle);
      }

      if (effectname == "Running Lights")
      {
        pref.putString("Effect", "running lights");
        xTaskCreate(this->Task1, "EffectThread", 10000, this, 5, &task1Handle);
      }

      if (effectname == "Fire")
      {
        pref.putString("Effect", "fire");
        xTaskCreate(this->Task1, "EffectThread", 10000, this, 5, &task1Handle);
      }
      pref.end();
    }
    
  }

  void StopEffect(){
    if(task1Handle!=NULL){
        vTaskDelete(task1Handle);
        vTaskDelay(200);
    }
  }

  static void Task1(void *pvParameters)
  {
    Preferences pref;
    pref.begin("RGB-Control", false);
    String currentEffect = pref.getString("Effect", "rainbow");
    if (currentEffect == "rainbow")
    {

      while (true)
      {

        rainbowCycle(10);
      }
    }

    if (currentEffect == "running lights")
    {
      while (true)
      {
        RunningLights(0xff, 0xff, 0x00, 50);
      }
    }

    if (currentEffect == "fire")
    {
      while (true)
      {
        Fire(55, 120, 15);
      }
    }
  }

  static void Fire(int Cooling, int Sparking, int SpeedDelay)
  {
    byte heat[LED_COUNT];
    int cooldown;

    // Step 1.  Cool down every cell a little
    for (int i = 0; i < LED_COUNT; i++)
    {
      cooldown = random(0, ((Cooling * 10) / LED_COUNT) + 2);

      if (cooldown > heat[i])
      {
        heat[i] = 0;
      }
      else
      {
        heat[i] = heat[i] - cooldown;
      }
    }

    // Step 2.  Heat from each cell drifts 'up' and diffuses a little
    for (int k = LED_COUNT - 1; k >= 2; k--)
    {
      heat[k] = (heat[k - 1] + heat[k - 2] + heat[k - 2]) / 3;
    }

    // Step 3.  Randomly ignite new 'sparks' near the bottom
    if (random(255) < Sparking)
    {
      int y = random(7);
      heat[y] = heat[y] + random(160, 255);
      //heat[y] = random(160,255);
    }

    // Step 4.  Convert heat to LED colors
    for (int j = 0; j < LED_COUNT; j++)
    {
      setPixelHeatColor(j, heat[j]);
    }

    FastLED.show();
    delay(SpeedDelay);
  }

  static void setPixelHeatColor(int Pixel, byte temperature)
  {
    // Scale 'heat' down from 0-255 to 0-191
    byte t192 = round((temperature / 255.0) * 191);

    // calculate ramp up from
    byte heatramp = t192 & 0x3F; // 0..63
    heatramp <<= 2;              // scale up to 0..252

    // figure out which third of the spectrum we're in:
    if (t192 > 0x80)
    { // hottest
      setPixel(Pixel, 255, 255, heatramp);
    }
    else if (t192 > 0x40)
    { // middle
      setPixel(Pixel, 255, heatramp, 0);
    }
    else
    { // coolest
      setPixel(Pixel, heatramp, 0, 0);
    }
  }

  static void RunningLights(byte red, byte green, byte blue, int WaveDelay)
  {
    int Position = 0;

    for (int j = 0; j < LED_COUNT * 2; j++)
    {
      Position++; // = 0; //Position + Rate;
      for (int i = 0; i < LED_COUNT; i++)
      {
        // sine wave, 3 offset waves make a rainbow!
        //float level = sin(i+Position) * 127 + 128;
        //setPixel(i,level,0,0);
        //float level = sin(i+Position) * 127 + 128;
        setPixel(i, ((sin(i + Position) * 127 + 128) / 255) * red,
                 ((sin(i + Position) * 127 + 128) / 255) * green,
                 ((sin(i + Position) * 127 + 128) / 255) * blue);
      }

      FastLED.show();
      delay(WaveDelay);
    }
  }

  static void rainbowCycle(int SpeedDelay)
  {
    byte *c;
    uint16_t i, j;

    for (j = 0; j < 256 * 5; j++)
    { // 5 cycles of all colors on wheel
      for (i = 0; i < LED_COUNT; i++)
      {
        c = Wheel(((i * 256 / LED_COUNT) + j) & 255);
        leds[i] = CRGB(*c, *(c + 1), *(c + 2));
      }
      FastLED.show();
      delay(SpeedDelay);
    }
  }

  static byte *Wheel(byte WheelPos)
  {
    static byte c[3];

    if (WheelPos < 85)
    {
      c[0] = WheelPos * 3;
      c[1] = 255 - WheelPos * 3;
      c[2] = 0;
    }
    else if (WheelPos < 170)
    {
      WheelPos -= 85;
      c[0] = 255 - WheelPos * 3;
      c[1] = 0;
      c[2] = WheelPos * 3;
    }
    else
    {
      WheelPos -= 170;
      c[0] = 0;
      c[1] = WheelPos * 3;
      c[2] = 255 - WheelPos * 3;
    }

    return c;
  }

  static void setAll(byte red, byte green, byte blue)
  {
    for (int i = 0; i < LED_COUNT; i++)
    {
      leds[i] = CRGB(red, green, blue);
    }
    FastLED.show();
  }

  static void setPixel(int Pixel, byte red, byte green, byte blue)
  {

    leds[Pixel].r = red;
    leds[Pixel].g = green;
    leds[Pixel].b = blue;
  }
};

void setup()
{
  Serial.begin(115200);
  Serial.print("start...");

  Preferences preferences; //define Preferences Object
  preferences.begin("RGB-Control", false);

  LED_COUNT = preferences.getString("LEDcount", "100").toInt();

  leds = new CRGB[LED_COUNT]; //init chrgb

  FastLED.addLeds<CHIPSET, LED_PIN, COLOR_ORDER>(leds, LED_COUNT).setCorrection(TypicalLEDStrip);
  //FastLED.setMaxPowerInVoltsAndMilliamps(5, 350);
  FastLED.clear(); // clear all pixel data

  FastLED.setBrightness(50);
  FastLED.show(); // display this frame

  //init Device Name

  String defaultname = "RGB Strip"; //default value

  String devicename = preferences.getString("Name", defaultname);

  //convert to char array
  int str_len = devicename.length() + 1;
  char char_array[str_len];
  devicename.toCharArray(char_array, str_len);
  //end init Device

  preferences.end();

  BLEDevice::init(char_array);
  BLEServer *pServer = BLEDevice::createServer();

  BLEService *pService = pServer->createService(SERVICE_UUID);

  BLEService *Settings = pServer->createService(SETTINGS_SERVICE_UUID);

  BLECharacteristic *changeDeviceName = Settings->createCharacteristic(
      CHANGE_DEVICENAME_UUID,
      BLECharacteristic::PROPERTY_WRITE);

  BLECharacteristic *changeNeoPixelNumber = Settings->createCharacteristic(
      NEOPIXEL_NUMBER_UUID,
      BLECharacteristic::PROPERTY_READ |
          BLECharacteristic::PROPERTY_WRITE);

  BLECharacteristic *restartDevice = Settings->createCharacteristic(
      RESTART_UUID,
      BLECharacteristic::PROPERTY_WRITE);


  BLECharacteristic *rgbCharacteristic = pService->createCharacteristic(
      RGB_CHARACTERISTIC_UUID,
      BLECharacteristic::PROPERTY_READ |
          BLECharacteristic::PROPERTY_WRITE);
  
  BLECharacteristic *effectCharacteristic = pService->createCharacteristic(
      EFFECT_UUID,
      BLECharacteristic::PROPERTY_READ |
          BLECharacteristic::PROPERTY_WRITE);

  String numbPixels = String(LED_COUNT);
  char chararray[str_len];
  numbPixels.toCharArray(chararray, str_len);

  rgbCharacteristic->setCallbacks(new MyCallbacks());
  effectCharacteristic->setCallbacks(new EffectCallback());
  changeDeviceName->setCallbacks(new DeviceNameChange());
  restartDevice->setCallbacks(new RestartCallback());
  changeNeoPixelNumber->setCallbacks(new NeoPixelChangeCallback());
  changeNeoPixelNumber->setValue(chararray);
  rgbCharacteristic->setValue("255, 255, 255");
  pService->start();
  Settings->start();
  BLEAdvertising *pAdvertising = pServer->getAdvertising();
  BLEAdvertisementData advert;
  advert.setManufacturerData("control");
  pAdvertising->setAdvertisementData(advert);
  pAdvertising->start();
}

void loop(){}