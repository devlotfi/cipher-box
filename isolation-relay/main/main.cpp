#include <Arduino.h>
#include "BluetoothSerial.h"
#include "esp_mac.h"
#include "esp_log.h"

BluetoothSerial SerialBT;

uint8_t btAddress[6] = {0xAA, 0xBB, 0xCC, 0xDD, 0xEE, 0x01};

static constexpr uint32_t UART_BAUD = 921600;
static constexpr size_t UART_RX_BUF = 16 * 1024;
static constexpr size_t UART_TX_BUF = 16 * 1024;
static constexpr size_t CHUNK_SIZE = 2048;
static constexpr uint32_t TASK_STACK = 4096;
static constexpr UBaseType_t TASK_PRIO = 5;

static void usbToBtTask(void *)
{
  static uint8_t buf[CHUNK_SIZE];

  for (;;)
  {
    int avail = Serial.available();
    if (avail <= 0)
    {
      vTaskDelay(1);
      continue;
    }

    size_t n = Serial.readBytes(buf, min((size_t)avail, CHUNK_SIZE));
    if (n == 0)
      continue;

    if (!SerialBT.hasClient())
      continue; // nobody connected: drop data so the UART buffer doesn't fill up

    size_t sent = 0;
    while (sent < n)
    {
      size_t w = SerialBT.write(buf + sent, n - sent);
      if (w == 0)
      {
        if (!SerialBT.hasClient())
          break;       // disconnected mid-write, discard the rest
        vTaskDelay(1); // congested, back off briefly
      }
      sent += w;
    }
  }
}

static void btToUsbTask(void *)
{
  static uint8_t buf[CHUNK_SIZE];

  for (;;)
  {
    int avail = SerialBT.available();
    if (avail <= 0)
    {
      vTaskDelay(1);
      continue;
    }

    size_t n = SerialBT.readBytes(buf, min((size_t)avail, CHUNK_SIZE));
    if (n > 0)
      Serial.write(buf, n); // blocks if TX ring is full -> natural backpressure
  }
}

void setup()
{
  Serial.setRxBufferSize(UART_RX_BUF);
  Serial.setTxBufferSize(UART_TX_BUF);
  Serial.begin(UART_BAUD);
  delay(2000);

  esp_err_t err = esp_iface_mac_addr_set(btAddress, ESP_MAC_BT);
  if (err != ESP_OK)
  {
    Serial.printf("MAC set failed: %s\n", esp_err_to_name(err));
    while (true)
      delay(1000);
  }

  if (!SerialBT.begin("ESP32-Relay"))
  {
    Serial.println("BT init failed");
    while (true)
      delay(1000);
  }

  uint8_t mac[6];
  esp_read_mac(mac, ESP_MAC_BT);

  Serial.printf(
      "BT MAC: %02X:%02X:%02X:%02X:%02X:%02X\n",
      mac[0], mac[1], mac[2],
      mac[3], mac[4], mac[5]);

  // One task per direction, both on core 1 (BT stack lives on core 0)
  xTaskCreatePinnedToCore(usbToBtTask, "usb2bt", TASK_STACK, nullptr, TASK_PRIO, nullptr, 1);
  xTaskCreatePinnedToCore(btToUsbTask, "bt2usb", TASK_STACK, nullptr, TASK_PRIO, nullptr, 1);
}

void loop()
{
  vTaskDelete(NULL); // the loop task is no longer needed
}