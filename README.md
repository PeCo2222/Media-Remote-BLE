# ESP32-S3 USB HID & Wi-Fi Media Remote

Een compacte, dual-mode afstandsbediening gebouwd met een **ESP32-S3 N16R8**. Het apparaat sluit via de native **USB-OTG-poort** aan op een Windows 10-pc en gedraagt zich als een standaard bedrade USB-muis, USB-toetsenbord en media-controller — zonder dat er Bluetooth op de pc nodig is.

Je kunt de pc bedienen via **fysieke drukknoppen (GPIO)** én draadloos via een **ingebouwde webinterface op poort 2222** vanaf je smartphone.

---

## Functionaliteiten

* **Skip Ad (Macro):** Verplaatst de muiscursor eerst naar een vaste buitenhoek van het scherm (zodat de startpositie altijd exact gelijk is, ook bij een dual-monitor opstelling), beweegt daarna naar de *Skip Ad*-knop en voert een linkermuisklik uit.
* **Play / Pause:** Stuurt een spatiebalk-aanslag (`Space`) naar de actieve videospeler.
* **Volume Regeling:** Stuurt hardwarematige USB Consumer Control-signalen (`Volume +` / `Volume -`) om het hoofdvolume van Windows 10 aan te passen.
* **Fullscreen & Ondertiteling:** Sneltoetsen voor volledig scherm (`F`) en ondertiteling aan/uit (`C`).
* **Visuele Status-LED:** Gebruikt de ingebouwde RGB NeoPixel op de ESP32-S3 om statussen en acties met kleuren aan te geven.
* **Veilige Configuratie:** Wi-Fi-gegevens zijn gescheiden van de hoofdcode via `secrets.h` en het bordje gebruikt een vast (statisch) IP-adres.

---

## Benodigde Hardware

* **Microcontroller:** ESP32-S3 N16R8 DevKit (met native USB-OTG-poort)
* **Kabel:** USB-C datakabel (aangesloten op de **`USB` / OTG-poort** van het bordje, *niet* de `UART`-poort)
* **Drukknoppen:** 2x momentane drukknop (optioneel, voor fysieke bediening naast de webinterface)

### Aansluitschema (GPIO)

| Component | ESP32-S3 Pin | Functie |
| :--- | :--- | :--- |
| **Knop 1** | `GPIO 4` naar `GND` | Skip Ad (Muis verplaatsen + Linkermuisklik) |
| **Knop 2** | `GPIO 5` naar `GND` | Play / Pause (`Spatiebalk`) |
| **Onboard NeoPixel** | `GPIO 48` | Status- en actie-indicator |

*(Opmerking: Interne pull-up weerstanden zijn in de code geactiveerd; externe weerstanden zijn niet nodig.)*

---

## NeoPixel Kleurcodes

| Kleur | Betekenis |
| :--- | :--- |
| **Blauw** | Opstarten en verbinden met Wi-Fi |
| **Dim Groen** | Verbonden en klaar voor gebruik |
| **Paars (flits)** | *Skip Ad* uitgevoerd |
| **Cyaan (flits)** | *Play / Pause* (`Space`) uitgevoerd |
| **Fel Groen (flits)** | *Volume Omhoog* |
| **Geel (flits)** | *Volume Omlaag* |
| **Blauw (flits)** | *Fullscreen* (`F`) |
| **Oranje (flits)** | *Ondertiteling* (`C`) |

---

## Software & Installatie

### 1. Benodigde Arduino Libraries
Installeer via de Arduino Library Manager:
* **Adafruit NeoPixel** (door Adafruit)
* Ingebouwde ESP32-libraries: `WiFi.h`, `WebServer.h`, `USB.h`, `USBHIDMouse.h`, `USBHIDKeyboard.h`, `USBHIDConsumerControl.h`

### 2. Arduino IDE Bordinstellingen
Selecteer onder **Tools** de volgende instellingen voor de **ESP32-S3 N16R8**:
* **Board:** `ESP32S3 Dev Module`
* **USB Mode:** `USB-OTG (TinyUSB)` *(Verplicht voor HID-functionaliteit)*
* **USB CDC On Boot:** `Enabled`
* **PSRAM:** `OPI PSRAM`
* **Flash Size:** `16MB (128Mb)`

### 3. Wi-Fi & Vast IP Configureren
1. Maak een bestand **`secrets.h`** aan in dezelfde map als je `.ino`-bestand:
   ```cpp
   #ifndef SECRETS_H
   #define SECRETS_H

   #define SECRET_SSID "JOUW_WIFI_NAAM"
   #define SECRET_PASS "JOUW_WIFI_WACHTWOORD"

   #endif
   ```
2. Pas in het hoofdprogramma (`local_IP` en `gateway`) de IP-instellingen aan zodat ze overeenkomen met jouw thuisnetwerk (bijv. `192.168.1.200`).

---

## Gebruik & Kalibratie (Windows 10)

### Windows 10 Muisinstelling
Voor een 100% consistente muispositie bij elke klik:
1. Open **Instellingen > Apparaten > Muis > Extra muisopties**.
2. Ga naar het tabblad **Opties voor de aanwijzer**.
3. Zet het vinkje uit bij **Precisie van de aanwijzer verbeteren** en klik op **Toepassen**.

### Webinterface Openen
1. Sluit de ESP32-S3 via de **USB-OTG-poort** aan op je pc.
2. Open op je smartphone (verbonden met hetzelfde Wi-Fi-netwerk) de browser en ga naar:
   ```text
   http://192.168.1.200:2222
   ```
3. *(Tip)* Kies in je mobiele browser voor **"Toevoegen aan startscherm"** om de remote als app-icoon te bewaren.

### Muiscoördinaten Aanpassen (Dual-Monitor)
Bij meerdere schermen duwt de macro de cursor eerst naar een buitenhoek die niet grenst aan het andere scherm (bijv. `(-127, -127)` voor de linkerbovenhoek van het linkerscherm) en beweegt daarna met vaste stappen naar de *Skip Ad*-knop. Pas in de code deze drie constanten aan voor jouw schermresolutie:
```cpp
const int MOVE_RIGHT_STEPS = 110;
const int MOVE_DOWN_STEPS  = 80;
const int MOVE_REPEAT      = 10;
```

---

## Projectstructuur

```text
├── ESP32S3_Media_Remote.ino   # Hoofdprogramma (USB HID, WebServer & GPIO)
├── secrets.h                  # Wi-Fi inloggegevens (niet delen / in .gitignore)
└── README.md                  # Projectdocumentatie
```
