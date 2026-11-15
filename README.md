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
└── README.md                    # Projectdocumentatie (BLE setup)
```
