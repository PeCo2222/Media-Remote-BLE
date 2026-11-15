# ESP32-S3 USB HID & BLE Media Remote

Een draadloze afstandsbediening op basis van een **ESP32-S3 N16R8** en de **Media Remote** Android-app (verbonden via **Bluetooth Low Energy / BLE**). De ESP32-S3 sluit via de native **USB-OTG-poort** aan op je Windows-pc en gedraagt zich als een standaard bedrade USB-muis, USB-toetsenbord en media-controller — zonder dat je pc zelf Bluetooth nodig heeft en zonder Wi-Fi, vast IP-adres of `secrets.h`.

Je kunt de pc bedienen via de **Android BLE-app** (knoppenremote, trackpad, scrollwiel en toetsenbord-invoer) én via optionele **fysieke drukknoppen (GPIO)**.

---

## Functionaliteiten

* **Directe BLE-verbinding (Nordic UART Service):** Verbindt rechtstreeks vanaf je Android-telefoon (zoals de Samsung Galaxy A26) met `ESP32S3-MediaRemote` zonder router, Wi-Fi-wachtwoord of HTTP-poort.
* **Skip Ad (Macro met schermkalibratie):** Verplaatst de muiscursor eerst naar de linkerbovenhoek `(-127, -127)` voor een consistente startpositie (ook bij dual-monitor opstellingen), beweegt daarna exact naar de *Skip Ad*-knop en voert een linkermuisklik uit. Profielen voor **LG**, **ROG** en **Custom** worden opgeslagen in het ESP32-flashgeheugen (`Preferences`) en zijn rechtstreeks vanuit de app te testen en op te slaan.
* **Media & Navigatie:** *Play / Pause* (`Space`), *- 5 sec* / *+ 5 sec* (Pijl Links/Rechts), *Key Up* / *Key Down*, *Fullscreen* (`F`) en *Subtitles* (`C`).
* **Volume Regeling:** Stuurt hardwarematige USB Consumer Control-signalen (`Volume +` / `Volume -`) om het hoofdvolume van Windows aan te passen.
* **HID Trackpad & Toetsenbord (in Android-app):** Beweeg de pc-muiscursor via het touchscreen (`M:dx,dy`), scroll verticaal (`SCR:dy`), voer linker- en rechtermuisklikken uit (`CLK:L` / `CLK:R`) en stuur sneltoetsen of volledige tekst (`TXT:...`) naar de pc.
* **Visuele Status-LED:** De ingebouwde RGB NeoPixel op de ESP32-S3 toont de BLE-verbindingsstatus en geeft kleurfeedback bij elke actie.

---

## Benodigde Hardware

* **Microcontroller:** ESP32-S3 N16R8 DevKit (met native USB-OTG-poort)
* **Kabel:** USB-C datakabel aangesloten op de **`USB` / OTG-poort** van het bordje (*niet* de `UART`-poort)
* **Drukknoppen (optioneel):** Momentane drukknoppen voor fysieke bediening of BLE-reset

### Aansluitschema (GPIO)

| Component | ESP32-S3 Pin | Functie |
| :--- | :--- | :--- |
| **Knop 1** | `GPIO 4` naar `GND` | Skip Ad (Muis verplaatsen + Linkermuisklik) |
| **Knop 2** | `GPIO 5` naar `GND` | Play / Pause (`Spatiebalk`) |
| **Knop 3 (optioneel)** | `GPIO 6` naar `GND` | BLE Reset (3 sec. ingedrukt houden verbreekt client & herstart advertising) |
| **Onboard NeoPixel** | `GPIO 48` | BLE-status- en actie-indicator |

*(Opmerking: Interne pull-up weerstanden (`INPUT_PULLUP`) zijn in de code geactiveerd; externe weerstanden zijn niet nodig.)*

---

## NeoPixel Kleurcodes

| Kleur | Betekenis |
| :--- | :--- |
| **Dim Blauw** | Opstarten en adverteren via BLE (wacht op telefoon) |
| **Dim Groen** | Verbonden via BLE en klaar voor gebruik |
| **Paars (flits)** | *Skip Ad* uitgevoerd |
| **Cyaan (flits)** | *Play / Pause* (`Space`) uitgevoerd |
| **Fel Groen (flits)** | *Volume Omhoog* |
| **Geel (flits)** | *Volume Omlaag* |
| **Blauw (flits)** | *Fullscreen* (`F`) |
| **Oranje (flits)** | *Ondertiteling* (`C`) of Pijltoetsen (`-5s`, `+5s`, `Up`, `Down`) |

---

## Software & Installatie (ESP32-S3)

### 1. Benodigde Arduino Libraries
Installeer via de Arduino Library Manager:
* **Adafruit NeoPixel** (door Adafruit)
* Ingebouwde ESP32-libraries: `BLEDevice.h`, `BLEServer.h`, `BLEUtils.h`, `BLE2902.h`, `Preferences.h`, `USB.h`, `USBHIDMouse.h`, `USBHIDKeyboard.h`, `USBHIDConsumerControl.h`

### 2. Arduino IDE Bordinstellingen
Selecteer onder **Tools** de volgende instellingen voor de **ESP32-S3 N16R8**:
* **Board:** `ESP32S3 Dev Module`
* **USB Mode:** `USB-OTG (TinyUSB)` *(Verplicht voor USB HID muis/toetsenbord)*
* **USB CDC On Boot:** `Enabled`
* **PSRAM:** `OPI PSRAM`
* **Flash Size:** `16MB (128Mb)`

### 3. Firmware Flashen
* Open de **`?` Handleiding** in de **Media Remote** Android-app en tik op **Copy Code** om de volledige BLE-sketch te kopiëren, of plak de BLE-sketch in Arduino IDE en upload via USB naar je ESP32-S3.
* Je hebt geen Wi-Fi-gegevens of `secrets.h` meer nodig.

---

## BLE GATT Configuratie

De Android-app en de ESP32-S3 gebruiken standaard het **Nordic UART Service (NUS)** profiel:

| Onderdeel | Waarde |
| :--- | :--- |
| **BLE Device Name** | `ESP32S3-MediaRemote` |
| **Service UUID** | `6E400001-B5A3-F393-E0A9-E50E24DCCA9E` |
| **RX Characteristic (Write)** | `6E400002-B5A3-F393-E0A9-E50E24DCCA9E` |
| **TX Characteristic (Notify)** | `6E400003-B5A3-F393-E0A9-E50E24DCCA9E` |

*(Gebruik je eigen UUID's of andere commando-strings? In de app onder **Instellingen `⚙`** kun je zowel de UUID's als elke knop-payload aanpassen, of **Auto-detect writable characteristic** ingeschakeld laten.)*

---

## Gebruik & Kalibratie (Android + Windows)

### Windows Muisinstelling
Voor een 100% consistente muispositie bij elke *Skip Ad*-klik:
1. Open **Instellingen > Bluetooth en apparaten > Muis > Extra muisopties**.
2. Ga naar het tabblad **Opties voor de aanwijzer**.
3. Zet het vinkje uit bij **Precisie van de aanwijzer verbeteren** en klik op **Toepassen**.

### Verbinden met de Android-app
1. Sluit de ESP32-S3 via de **USB-OTG-poort** aan op je pc (de NeoPixel brandt dim blauw).
2. Open **Media Remote** op je Android-telefoon en tik onderaan op het **Bluetooth-icoon**.
3. Geef eenmalig toestemming voor *Apparaten in de buurt* (op Android 12+ is geen locatietoestemming nodig).
4. Kies **`ESP32S3-MediaRemote`** in de scannerlijst. De NeoPixel wordt dim groen en de app onthoudt het apparaat voor snelle herverbinding met één tik.

### Schermkalibratie & Trackpad
* **Skip Ad kalibreren:** Open **Instellingen (`⚙`) > Skip Ad Screen Calibration (BLE)** om de `X Steps` en `Y Steps` voor **LG Ultrawide**, **17" ROG Laptop** of **Custom Screen** te testen (**Test**) en direct op de ESP32-S3 op te slaan (**Save & Default**).
* **Trackpad & Keys:** Schakel bovenaan in de app naar **Trackpad & Keys** om je telefoon als draadloze USB-muis, scrollwiel en toetsenbord te gebruiken.

---

## Overzicht BLE Commando's

| Commando (`TX -> ESP32`) | Actie op PC / ESP32 |
| :--- | :--- |
| `SKIP` | Voert *Skip Ad* macro uit voor het actieve standaardscherm |
| `SKIP_TEST:x,y` | Test een *Skip Ad* cursorbeweging zonder op te slaan |
| `SKIP_SAVE:scr,x,y` | Slaat `x,y` op voor `lg` / `rog` / `custom` en stelt als standaard in |
| `PLAY` | Spatiebalk (Play / Pause) |
| `BACK5` / `FWD5` | Pijl Links / Pijl Rechts (-5s / +5s) |
| `UP` / `DOWN` | Pijl Omhoog / Pijl Omlaag |
| `VOLDN` / `VOLUP` | USB Consumer Volume - / + |
| `FULL` / `SUBS` | Toets `f` (Fullscreen) / `c` (Ondertiteling) |
| `M:dx,dy` | Relatieve muisbeweging (trackpad) |
| `CLK:L` / `CLK:R` | Linkermuis- / rechtermuisklik |
| `SCR:dy` | Verticaal scrollwiel |
| `ESC` / `TAB` / `ENTER` / `SPACE` | Directe toetsaanslagen |
| `TXT:<tekst>` | Typt willekeurige tekst via USB Keyboard |
| `KRAW:<mod>,<key>` | Raw USB HID keycode (o.a. `@`, `#`, `<`, `>`, `\` op AZERTY) |

---

## Projectstructuur

```text
├── app/                         # Android Jetpack Compose BLE Media Remote app
│   ├── src/main/java/com/example/
│   │   ├── ble/                 # BLE GATT-client, scanner & schrijf-wachtrij
│   │   ├── data/                # DataStore voorkeuren (UUID's, payloads, kalibratie)
│   │   ├── model/               # Remote-acties, BLE-configuratie & verbindingsstatus
│   │   ├── viewmodel/           # RemoteViewModel & trackpad delta-throttling
│   │   └── ui/screens/          # RemoteMainScreen, BleScannerSheet, SettingsScreen, HelpScreen
├── esp32/
│   └── ESP32S3_BLE_MediaRemote/
│       └── ESP32S3_BLE_MediaRemote.ino   # ESP32-S3 BLE + USB HID sketch voor Arduino IDE
└── README.md                    # Projectdocumentatie (BLE setup)
```

---

## ESP32-S3 Arduino Sketch (`.ino`)

```cpp
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
```

