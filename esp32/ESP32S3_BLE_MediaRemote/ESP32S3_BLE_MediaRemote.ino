// Board: ESP32S3 Dev Module (USB Mode: USB-OTG / TinyUSB)
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include "USB.h"
#include "USBHIDMouse.h"
#include "USBHIDKeyboard.h"
#include "USBHIDConsumerControl.h"
#include <Adafruit_NeoPixel.h>
#include <Preferences.h>

// --- BLE NORDIC UART SERVICE (Matches Media Remote App) ---
#define BLE_DEVICE_NAME "ESP32S3-MediaRemote"
#define SERVICE_UUID    "6E400001-B5A3-F393-E0A9-E50E24DCCA9E"
#define RX_UUID         "6E400002-B5A3-F393-E0A9-E50E24DCCA9E"
#define TX_UUID         "6E400003-B5A3-F393-E0A9-E50E24DCCA9E"

// --- HARDWARE PINS ---
#define NEOPIXEL_PIN      48
#define BTN_SKIP_PIN      4
#define BTN_PAUSE_PIN     5
#define BTN_BLE_RESET_PIN 6  // Wired to GND; hold 3s to drop client & re-advertise

// --- MOUSE CALIBRATION ---
const int MOVE_REPEAT = 10;

Preferences prefs;

USBHIDMouse Mouse;
USBHIDKeyboard Keyboard;
USBHIDConsumerControl ConsumerControl;
Adafruit_NeoPixel pixel(1, NEOPIXEL_PIN, NEO_GRB + NEO_KHZ800);

BLEServer *pServer = nullptr;
BLECharacteristic *pTxChar = nullptr;
volatile bool deviceConnected = false;
volatile bool shouldRestartAdv = false;

// Saved Skip Ad coordinates
int lgX, lgY;
int rogX, rogY;
int customX, customY;
String defaultScreen;

// Thread-safe queue so BLE onWrite() returns immediately
struct CmdPacket {
  char text[96];
};
QueueHandle_t cmdQueue;

void setLed(uint8_t r, uint8_t g, uint8_t b) {
  pixel.setPixelColor(0, pixel.Color(r, g, b));
  pixel.show();
}

void restoreIdleLed() {
  if (deviceConnected) {
    setLed(0, 30, 0);  // Dim Green = BLE Connected
  } else {
    setLed(0, 0, 80);  // Dim Blue = Advertising / Waiting
  }
}

void flashLed(uint8_t r, uint8_t g, uint8_t b) {
  setLed(r, g, b);
  delay(120);
  restoreIdleLed();
}

void sendBleStatus(const String &msg) {
  if (deviceConnected && pTxChar != nullptr) {
    pTxChar->setValue(msg.c_str());
    pTxChar->notify();
  }
}

// --- MEDIA & HID ACTIONS ---
void triggerSkipAd(int totalX, int totalY) {
  setLed(150, 0, 150); // Purple flash

  // 1. Reset cursor to top-left corner
  for (int i = 0; i < 40; i++) {
    Mouse.move(-127, -127);
    delay(5);
  }

  // 2. Distribute steps evenly across MOVE_REPEAT (10) steps
  int stepX = totalX / MOVE_REPEAT;
  int remX  = totalX % MOVE_REPEAT;
  int stepY = totalY / MOVE_REPEAT;
  int remY  = totalY % MOVE_REPEAT;

  for (int i = 0; i < MOVE_REPEAT; i++) {
    int moveX = stepX + (i < abs(remX) ? (remX > 0 ? 1 : -1) : 0);
    int moveY = stepY + (i < abs(remY) ? (remY > 0 ? 1 : -1) : 0);
    Mouse.move(moveX, moveY);
    delay(10);
  }

  delay(30);
  Mouse.click(MOUSE_LEFT);

  delay(150);
  restoreIdleLed();
}

// Overload for physical BTN_SKIP_PIN & default BLE "SKIP" command
void triggerSkipAd() {
  if (defaultScreen == "rog") {
    triggerSkipAd(rogX, rogY);
  } else if (defaultScreen == "custom") {
    triggerSkipAd(customX, customY);
  } else {
    triggerSkipAd(lgX, lgY);
  }
}

void triggerPlayPause() {
  Keyboard.write(' ');
  flashLed(0, 150, 150); // Cyan
}

void triggerVolUp() {
  ConsumerControl.press(CONSUMER_CONTROL_VOLUME_INCREMENT);
  ConsumerControl.release();
  flashLed(0, 150, 0); // Green
}

void triggerVolDown() {
  ConsumerControl.press(CONSUMER_CONTROL_VOLUME_DECREMENT);
  ConsumerControl.release();
  flashLed(150, 150, 0); // Yellow
}

void triggerFullscreen() {
  Keyboard.write('f');
  flashLed(0, 80, 200); // Blue
}

void triggerSubtitles() {
  Keyboard.write('c');
  flashLed(200, 80, 0); // Orange
}

void triggerBack5sec() {
  Keyboard.write(KEY_LEFT_ARROW);
  flashLed(200, 80, 0);
}

void triggerForward5sec() {
  Keyboard.write(KEY_RIGHT_ARROW);
  flashLed(200, 80, 0);
}

void triggerKeyDown() {
  Keyboard.write(KEY_DOWN_ARROW);
  flashLed(200, 80, 0);
}

void triggerKeyUp() {
  Keyboard.write(KEY_UP_ARROW);
  flashLed(200, 80, 0);
}

void handleCommand(String cmd) {
  cmd.trim();
  if (cmd.length() == 0) return;

  // Media Remote actions (accepts both BLE short codes and old HTTP paths)
  if (cmd == "SKIP" || cmd == "/skip") {
    triggerSkipAd();
    sendBleStatus("Action: Skip Ad (" + defaultScreen + ")");
  } else if (cmd.startsWith("SKIP_TEST:")) {
    int comma = cmd.indexOf(',');
    if (comma > 10) {
      int x = cmd.substring(10, comma).toInt();
      int y = cmd.substring(comma + 1).toInt();
      triggerSkipAd(x, y);
      sendBleStatus("Test | X:" + String(x) + " Y:" + String(y));
    }
  } else if (cmd.startsWith("SKIP_SAVE:")) {
    // Format: SKIP_SAVE:lg,990,350
    int c1 = cmd.indexOf(',');
    int c2 = cmd.indexOf(',', c1 + 1);
    if (c1 > 10 && c2 > c1) {
      String scr = cmd.substring(10, c1);
      scr.toLowerCase();
      int x = cmd.substring(c1 + 1, c2).toInt();
      int y = cmd.substring(c2 + 1).toInt();
      if (scr == "lg" || scr == "rog" || scr == "custom") {
        defaultScreen = scr;
        prefs.putString("def_scr", defaultScreen);
        if (scr == "rog") {
          rogX = x; rogY = y;
          prefs.putInt("rog_x", rogX);
          prefs.putInt("rog_y", rogY);
        } else if (scr == "custom") {
          customX = x; customY = y;
          prefs.putInt("cust_x", customX);
          prefs.putInt("cust_y", customY);
        } else {
          lgX = x; lgY = y;
          prefs.putInt("lg_x", lgX);
          prefs.putInt("lg_y", lgY);
        }
        sendBleStatus("Saved " + scr + " | X:" + String(x) + " Y:" + String(y));
      }
    }
  } else if (cmd == "PLAY" || cmd == "/pause") {
    triggerPlayPause();
    sendBleStatus("Action: Play / Pause");
  } else if (cmd == "BACK5" || cmd == "/back5sec") {
    triggerBack5sec();
    sendBleStatus("Action: -5 sec");
  } else if (cmd == "FWD5" || cmd == "/forward5sec") {
    triggerForward5sec();
    sendBleStatus("Action: +5 sec");
  } else if (cmd == "UP" || cmd == "/keyup") {
    triggerKeyUp();
    sendBleStatus("Action: Key Up");
  } else if (cmd == "DOWN" || cmd == "/keydown") {
    triggerKeyDown();
    sendBleStatus("Action: Key Down");
  } else if (cmd == "VOLUP" || cmd == "/volup") {
    triggerVolUp();
    sendBleStatus("Action: Volume Up");
  } else if (cmd == "VOLDN" || cmd == "/voldown") {
    triggerVolDown();
    sendBleStatus("Action: Volume Down");
  } else if (cmd == "FULL" || cmd == "/fullscreen") {
    triggerFullscreen();
    sendBleStatus("Action: Fullscreen (F)");
  } else if (cmd == "SUBS" || cmd == "/subs") {
    triggerSubtitles();
    sendBleStatus("Action: Subtitles (C)");

  // Trackpad & Extra Keyboard HID actions
  } else if (cmd == "CLK:L") {
    Mouse.click(MOUSE_LEFT);
  } else if (cmd == "CLK:R") {
    Mouse.click(MOUSE_RIGHT);
  } else if (cmd == "ESC") {
    Keyboard.write(KEY_ESC);
  } else if (cmd == "TAB") {
    Keyboard.write(KEY_TAB);
  } else if (cmd == "ENTER") {
    Keyboard.write(KEY_RETURN);
  } else if (cmd == "SPACE") {
    Keyboard.write(' ');
  } else if (cmd.startsWith("M:")) {
    int comma = cmd.indexOf(',');
    if (comma > 2) {
      int dx = constrain(cmd.substring(2, comma).toInt(), -127, 127);
      int dy = constrain(cmd.substring(comma + 1).toInt(), -127, 127);
      Mouse.move(dx, dy);
    }
  } else if (cmd.startsWith("SCR:")) {
    int dy = constrain(cmd.substring(4).toInt(), -127, 127);
    Mouse.move(0, 0, dy);
  } else if (cmd.startsWith("TXT:")) {
    Keyboard.print(cmd.substring(4));
    sendBleStatus("Typed text");
  } else if (cmd.startsWith("KRAW:")) {
    int comma = cmd.indexOf(',');
    if (comma > 5) {
      KeyReport report = {0};
      report.modifiers = (uint8_t)cmd.substring(5, comma).toInt();
      report.keys[0] = (uint8_t)cmd.substring(comma + 1).toInt();
      Keyboard.sendReport(&report);
      delay(12);
      KeyReport empty = {0};
      Keyboard.sendReport(&empty);
    }
  }
}

// --- BLE CALLBACKS ---
class ServerCallbacks : public BLEServerCallbacks {
  void onConnect(BLEServer *server) override {
    deviceConnected = true;
  }
  void onDisconnect(BLEServer *server) override {
    deviceConnected = false;
    shouldRestartAdv = true;
  }
};

class RxCallbacks : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic *pChar) override {
    String val = String(pChar->getValue().c_str());
    int start = 0;
    while (start < (int)val.length()) {
      int nl = val.indexOf('\n', start);
      if (nl == -1) nl = val.length();
      String line = val.substring(start, nl);
      line.trim();
      if (line.length() > 0) {
        CmdPacket pkt;
        line.toCharArray(pkt.text, sizeof(pkt.text));
        xQueueSend(cmdQueue, &pkt, 0);
      }
      start = nl + 1;
    }
  }
};

void setup() {
  Serial.begin(115200);
  pinMode(BTN_SKIP_PIN, INPUT_PULLUP);
  pinMode(BTN_PAUSE_PIN, INPUT_PULLUP);
  pinMode(BTN_BLE_RESET_PIN, INPUT_PULLUP);

  pixel.begin();
  setLed(0, 0, 80); // Blue while starting & advertising

  // Initialize USB HID devices
  Mouse.begin();
  Keyboard.begin();
  ConsumerControl.begin();
  USB.begin();

  // Load saved Skip Ad screen coordinates from flash
  prefs.begin("mouse_cfg", false);
  lgX     = prefs.getInt("lg_x", 990);
  lgY     = prefs.getInt("lg_y", 350);
  rogX    = prefs.getInt("rog_x", 1400);
  rogY    = prefs.getInt("rog_y", 400);
  customX = prefs.getInt("cust_x", 1200);
  customY = prefs.getInt("cust_y", 380);
  defaultScreen = prefs.getString("def_scr", "lg");

  cmdQueue = xQueueCreate(32, sizeof(CmdPacket));

  // Initialize BLE GATT Server
  BLEDevice::init(BLE_DEVICE_NAME);
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new ServerCallbacks());

  BLEService *pService = pServer->createService(SERVICE_UUID);
  pTxChar = pService->createCharacteristic(
    TX_UUID,
    BLECharacteristic::PROPERTY_NOTIFY
  );
  pTxChar->addDescriptor(new BLE2902());

  BLECharacteristic *pRxChar = pService->createCharacteristic(
    RX_UUID,
    BLECharacteristic::PROPERTY_WRITE | BLECharacteristic::PROPERTY_WRITE_NR
  );
  pRxChar->setCallbacks(new RxCallbacks());

  pService->start();

  BLEAdvertising *pAdv = BLEDevice::getAdvertising();
  pAdv->addServiceUUID(SERVICE_UUID);
  pAdv->setScanResponse(true);
  BLEDevice::startAdvertising();

  Serial.println("BLE Media Remote ready: " BLE_DEVICE_NAME);
}

unsigned long bleBtnPressStart = 0;
bool bleBtnHandled = false;

void loop() {
  if (shouldRestartAdv) {
    shouldRestartAdv = false;
    delay(200);
    BLEDevice::startAdvertising();
    restoreIdleLed();
  }

  static bool lastConn = false;
  if (deviceConnected != lastConn) {
    lastConn = deviceConnected;
    restoreIdleLed();
  }

  // Drain queued BLE commands safely on the main loop
  CmdPacket pkt;
  while (xQueueReceive(cmdQueue, &pkt, 0) == pdTRUE) {
    handleCommand(String(pkt.text));
  }

  // Physical Skip & Pause buttons
  if (digitalRead(BTN_SKIP_PIN) == LOW) {
    triggerSkipAd();
    delay(400);
  }
  if (digitalRead(BTN_PAUSE_PIN) == LOW) {
    triggerPlayPause();
    delay(400);
  }

  // Hold pin 6 for 3s to force-disconnect BLE client & re-advertise
  if (digitalRead(BTN_BLE_RESET_PIN) == LOW) {
    if (bleBtnPressStart == 0) {
      bleBtnPressStart = millis();
      bleBtnHandled = false;
    } else if (!bleBtnHandled && (millis() - bleBtnPressStart >= 3000)) {
      bleBtnHandled = true;
      for (int i = 0; i < 3; i++) {
        setLed(0, 0, 200);
        delay(120);
        setLed(0, 0, 0);
        delay(120);
      }
      if (deviceConnected) {
        pServer->disconnect(pServer->getConnId());
      }
      BLEDevice::startAdvertising();
      restoreIdleLed();
    }
  } else {
    bleBtnPressStart = 0;
    bleBtnHandled = false;
  }

  delay(5);
}
