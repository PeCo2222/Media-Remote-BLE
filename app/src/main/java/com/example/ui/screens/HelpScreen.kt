package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.BlePreset
import com.example.ui.theme.BleConnectedCyan
import com.example.ui.theme.MediaCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceHigh
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onBack: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ObsidianBg,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.help_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("help_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ObsidianBg)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Surface(
                    modifier = Modifier.widthIn(max = 640.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = ObsidianSurface,
                    border = BorderStroke(1.dp, ObsidianBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "How It Works",
                            style = MaterialTheme.typography.titleMedium,
                            color = MediaCyan
                        )
                        Text(
                            text = "1. Plug your ESP32-S3 into your PC's USB port (USB OTG/Native USB).\n" +
                                "2. Flash a BLE GATT server (Nordic UART Service by default) that forwards incoming commands to USBHIDKeyboard & USBHIDMouse.\n" +
                                "3. Tap the Bluetooth icon on the remote screen to scan & pair with your ESP32-S3 from anywhere in the room.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier.widthIn(max = 640.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = ObsidianSurface,
                    border = BorderStroke(1.dp, ObsidianBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Default BLE GATT UUIDs (Nordic UART)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MediaCyan
                        )
                        Text(
                            text = "Service: ${BlePreset.NORDIC_UART.serviceUuid}\n" +
                                "RX (Write): ${BlePreset.NORDIC_UART.writeUuid}\n" +
                                "TX (Notify): ${BlePreset.NORDIC_UART.notifyUuid}",
                            style = MaterialTheme.typography.labelMedium,
                            color = BleConnectedCyan
                        )
                        Text(
                            text = "Already using different UUIDs or HTTP command names? Open Settings to customize any UUID or button payload.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier.widthIn(max = 640.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = ObsidianSurface,
                    border = BorderStroke(1.dp, ObsidianBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Default Command Protocol",
                            style = MaterialTheme.typography.titleMedium,
                            color = MediaCyan
                        )
                        ProtocolRow("PLAY / SKIP", "Play/Pause (Space) · Skip Ad macro")
                        ProtocolRow("BACK5 / FWD5", "Left / Right Arrow (-5s / +5s)")
                        ProtocolRow("UP / DOWN", "Up / Down Arrow keys")
                        ProtocolRow("VOLDN / VOLUP", "Consumer Volume Down / Up")
                        ProtocolRow("FULL / SUBS", "Key 'f' (Fullscreen) / 'c' (Subtitles)")
                        ProtocolRow("SKIP_TEST:x,y", "Test Skip Ad cursor jump without saving")
                        ProtocolRow("SKIP_SAVE:scr,x,y", "Save lg/rog/custom coords & set default")
                        ProtocolRow("M:dx,dy", "Relative USB Mouse move (e.g. M:12,-4)")
                        ProtocolRow("CLK:L / CLK:R", "Mouse Left / Right click")
                        ProtocolRow("SCR:dy", "Mouse vertical scroll wheel")
                        ProtocolRow("TXT:<text>", "Type string via USB Keyboard")
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier.widthIn(max = 640.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = ObsidianSurface,
                    border = BorderStroke(1.dp, ObsidianBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "ESP32-S3 Arduino Sketch",
                                style = MaterialTheme.typography.titleMedium,
                                color = MediaCyan
                            )
                            Button(
                                onClick = {
                                    clipboard.setText(AnnotatedString(ESP32_S3_SAMPLE_SKETCH))
                                    Toast.makeText(
                                        context,
                                        "ESP32-S3 sketch copied to clipboard",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MediaCyan),
                                modifier = Modifier.testTag("copy_esp32_sketch_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ContentCopy,
                                    contentDescription = null,
                                    tint = TextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy Code", color = TextPrimary)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ObsidianSurfaceHigh,
                            border = BorderStroke(1.dp, ObsidianBorder)
                        ) {
                            Text(
                                text = ESP32_S3_SAMPLE_SKETCH,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProtocolRow(cmd: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = cmd,
            style = MaterialTheme.typography.labelMedium,
            color = BleConnectedCyan,
            modifier = Modifier.width(136.dp)
        )
        Text(
            text = desc,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
    }
}

private val ESP32_S3_SAMPLE_SKETCH = """
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
#define BTN_BLE_RESET_PIN 6

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

int lgX, lgY, rogX, rogY, customX, customY;
String defaultScreen;

struct CmdPacket {
  char text[96];
};
QueueHandle_t cmdQueue;

void setLed(uint8_t r, uint8_t g, uint8_t b) {
  pixel.setPixelColor(0, pixel.Color(r, g, b));
  pixel.show();
}

void restoreIdleLed() {
  if (deviceConnected) setLed(0, 30, 0); // Dim Green = BLE Connected
  else setLed(0, 0, 80);                 // Dim Blue  = Advertising
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

void triggerSkipAd(int totalX, int totalY) {
  setLed(150, 0, 150);
  for (int i = 0; i < 40; i++) {
    Mouse.move(-127, -127);
    delay(5);
  }
  int stepX = totalX / MOVE_REPEAT;
  int remX  = totalX % MOVE_REPEAT;
  int stepY = totalY / MOVE_REPEAT;
  int remY  = totalY % MOVE_REPEAT;
  for (int i = 0; i < MOVE_REPEAT; i++) {
    int mx = stepX + (i < abs(remX) ? (remX > 0 ? 1 : -1) : 0);
    int my = stepY + (i < abs(remY) ? (remY > 0 ? 1 : -1) : 0);
    Mouse.move(mx, my);
    delay(10);
  }
  delay(30);
  Mouse.click(MOUSE_LEFT);
  delay(150);
  restoreIdleLed();
}

void triggerSkipAd() {
  if (defaultScreen == "rog") triggerSkipAd(rogX, rogY);
  else if (defaultScreen == "custom") triggerSkipAd(customX, customY);
  else triggerSkipAd(lgX, lgY);
}

void handleCommand(String cmd) {
  cmd.trim();
  if (cmd.length() == 0) return;

  if (cmd == "SKIP" || cmd == "/skip") {
    triggerSkipAd();
    sendBleStatus("Skip Ad (" + defaultScreen + ")");
  } else if (cmd.startsWith("SKIP_TEST:")) {
    int c = cmd.indexOf(',');
    if (c > 10) {
      int x = cmd.substring(10, c).toInt();
      int y = cmd.substring(c + 1).toInt();
      triggerSkipAd(x, y);
      sendBleStatus("Test (" + String(x) + "," + String(y) + ")");
    }
  } else if (cmd.startsWith("SKIP_SAVE:")) {
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
        if (scr == "rog") { rogX = x; rogY = y; prefs.putInt("rog_x", x); prefs.putInt("rog_y", y); }
        else if (scr == "custom") { customX = x; customY = y; prefs.putInt("cust_x", x); prefs.putInt("cust_y", y); }
        else { lgX = x; lgY = y; prefs.putInt("lg_x", x); prefs.putInt("lg_y", y); }
        sendBleStatus("Saved " + scr + ": " + String(x) + "," + String(y));
      }
    }
  } else if (cmd == "PLAY" || cmd == "/pause") {
    Keyboard.write(' ');
    flashLed(0, 150, 150);
    sendBleStatus("Play / Pause");
  } else if (cmd == "BACK5" || cmd == "/back5sec") {
    Keyboard.write(KEY_LEFT_ARROW);
    flashLed(200, 80, 0);
    sendBleStatus("-5 sec");
  } else if (cmd == "FWD5" || cmd == "/forward5sec") {
    Keyboard.write(KEY_RIGHT_ARROW);
    flashLed(200, 80, 0);
    sendBleStatus("+5 sec");
  } else if (cmd == "UP" || cmd == "/keyup") {
    Keyboard.write(KEY_UP_ARROW);
    flashLed(200, 80, 0);
    sendBleStatus("Key Up");
  } else if (cmd == "DOWN" || cmd == "/keydown") {
    Keyboard.write(KEY_DOWN_ARROW);
    flashLed(200, 80, 0);
    sendBleStatus("Key Down");
  } else if (cmd == "VOLUP" || cmd == "/volup") {
    ConsumerControl.press(CONSUMER_CONTROL_VOLUME_INCREMENT);
    ConsumerControl.release();
    flashLed(0, 150, 0);
    sendBleStatus("Vol +");
  } else if (cmd == "VOLDN" || cmd == "/voldown") {
    ConsumerControl.press(CONSUMER_CONTROL_VOLUME_DECREMENT);
    ConsumerControl.release();
    flashLed(150, 150, 0);
    sendBleStatus("Vol -");
  } else if (cmd == "FULL" || cmd == "/fullscreen") {
    Keyboard.write('f');
    flashLed(0, 80, 200);
    sendBleStatus("Fullscreen (F)");
  } else if (cmd == "SUBS" || cmd == "/subs") {
    Keyboard.write('c');
    flashLed(200, 80, 0);
    sendBleStatus("Subtitles (C)");
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
    int c = cmd.indexOf(',');
    if (c > 2) {
      int dx = constrain(cmd.substring(2, c).toInt(), -127, 127);
      int dy = constrain(cmd.substring(c + 1).toInt(), -127, 127);
      Mouse.move(dx, dy);
    }
  } else if (cmd.startsWith("SCR:")) {
    int dy = constrain(cmd.substring(4).toInt(), -127, 127);
    Mouse.move(0, 0, dy);
  } else if (cmd.startsWith("TXT:")) {
    Keyboard.print(cmd.substring(4));
    sendBleStatus("Typed text");
  } else if (cmd.startsWith("KRAW:")) {
    int c = cmd.indexOf(',');
    if (c > 5) {
      KeyReport report = {0};
      report.modifiers = (uint8_t)cmd.substring(5, c).toInt();
      report.keys[0] = (uint8_t)cmd.substring(c + 1).toInt();
      Keyboard.sendReport(&report);
      delay(12);
      KeyReport empty = {0};
      Keyboard.sendReport(&empty);
    }
  }
}

class ServerCallbacks : public BLEServerCallbacks {
  void onConnect(BLEServer *s) override {
    deviceConnected = true;
  }
  void onDisconnect(BLEServer *s) override {
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
  setLed(0, 0, 80);

  Mouse.begin();
  Keyboard.begin();
  ConsumerControl.begin();
  USB.begin();

  prefs.begin("mouse_cfg", false);
  lgX     = prefs.getInt("lg_x", 990);
  lgY     = prefs.getInt("lg_y", 350);
  rogX    = prefs.getInt("rog_x", 1400);
  rogY    = prefs.getInt("rog_y", 400);
  customX = prefs.getInt("cust_x", 1200);
  customY = prefs.getInt("cust_y", 380);
  defaultScreen = prefs.getString("def_scr", "lg");

  cmdQueue = xQueueCreate(32, sizeof(CmdPacket));

  BLEDevice::init(BLE_DEVICE_NAME);
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new ServerCallbacks());

  BLEService *pService = pServer->createService(SERVICE_UUID);
  pTxChar = pService->createCharacteristic(TX_UUID, BLECharacteristic::PROPERTY_NOTIFY);
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

  CmdPacket pkt;
  while (xQueueReceive(cmdQueue, &pkt, 0) == pdTRUE) {
    handleCommand(String(pkt.text));
  }

  if (digitalRead(BTN_SKIP_PIN) == LOW) {
    triggerSkipAd();
    delay(400);
  }
  if (digitalRead(BTN_PAUSE_PIN) == LOW) {
    triggerPlayPause();
    delay(400);
  }

  if (digitalRead(BTN_BLE_RESET_PIN) == LOW) {
    if (bleBtnPressStart == 0) {
      bleBtnPressStart = millis();
      bleBtnHandled = false;
    } else if (!bleBtnHandled && (millis() - bleBtnPressStart >= 3000)) {
      bleBtnHandled = true;
      for (int i = 0; i < 3; i++) {
        setLed(0, 0, 200); delay(120);
        setLed(0, 0, 0);   delay(120);
      }
      if (deviceConnected) pServer->disconnect(pServer->getConnId());
      BLEDevice::startAdvertising();
      restoreIdleLed();
    }
  } else {
    bleBtnPressStart = 0;
    bleBtnHandled = false;
  }

  delay(5);
}
""".trimIndent()
