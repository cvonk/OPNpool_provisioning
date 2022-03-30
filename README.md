# Provisioning Library  

Provisioning library provides a mechanism to send network credentials and/or custom data to ESP32 (or its variants like S2, S3, C3, etc.) or ESP8266 devices.

This repository contains the source code for the companion Android app for this provisioning mechanism.
To get this app please clone this repository using the below command:
```
 git clone https://github.com/espressif/esp-idf-provisioning-android.git
```

- [Features](#features)  
- [Requirements](#requirements)  
- [How to include](#how-to-include)  
- [Usage](#using-ESPProvision)  
  - [****Introduction****](#introduction)  
  - [****Getting ESPDevice****](#getting-ESPDevice)  
  - [****Provisioning****](#provisioning)  
  - [****Other Configuration****](#other-configuration)
- [License](#license)  
  
## Features  
  
- [x] Search for available BLE devices.  
- [x] Scan device QR code to provide reference to ESP device.  
- [x] Create reference of ESPDevice manually.  
- [x] Data Encryption  
- [x] Data transmission through BLE and SoftAP.  
- [x] Scan for available Wi-Fi networks. 
- [x] Provision device.  
- [x] Scan for available Wi-Fi networks.  
- [x] Support for exchanging custom data.
  
## Requirements  
  
- Supports Android 6.0 (API level 23) and above.  

##  How to include  
  
 Add this in your root `build.gradle` at the end of repositories:
 ```
 allprojects {
	 repositories {
		 ...
		 maven { url 'https://jitpack.io' }
	 }
 }
 ```
And add a dependency code to your  app module's  `build.gradle`  file. 
```  
 implementation 'com.github.espressif:esp-idf-provisioning-android:lib-2.0.11'
```

## Using Provisioning Library
 ## Introduction    
 Provisioning library provides a simpler mechanism to communicate with an ESP-32, ESP32-S2 and ESP8266 devices. It gives an efficient search and scan model to listen and return devices which are in provisioning mode. It embeds security protocol and allow for safe transmission of data by doing end to end encryption. It supports BLE and SoftAP as mode of transmission which are configurable at runtime. Its primarily use is to provide home network credentials to a device and ensure device connectivity status is returned to the application.    
    
 ## Getting ESPDevice  
   
`ESPDevice` object is virtual representation of ESP-32/ESP32-S2/ESP8266 devices. It provides interface to interact with devices directly in a simpler manner.
`ESPProvisionManager` is a singleton class that encompasses APIs for searching ESP devices using BLE or SoftAP transport. Once app has received `ESPDevice` instance, app can maintain it for other API calls or it can receive same `ESPDevice` instance by calling API `getEspDevice()` of `ESPProvisionManager` class.

 `ESPDevice` instances can be obtained from two ways as described following : 
 
 ### QR Code Scan 
 Device information can be extracted from scanning valid QR code. API returns single `ESPDevice` instance on success. It supports both SoftAP and BLE.
 If your device does not have QR code, you can use any online QR code generator.
QR code payload is a JSON string representing a dictionary with key value pairs listed in the table below. An example payload :
`{"ver":"v1","name":"PROV_CE03C0","pop":"abcd1234","transport":"softap"}`

Payload information : 

| Key       	| Detail                             	| Values                                  	| Required                                                            	|
|-----------	|------------------------------------	|-----------------------------------------	|---------------------------------------------------------------------	|
| ver       	| Version of the QR code.            	| Currently, it must be v1.               	| Yes                                                                 	|
| name      	| Name of the device.                	| PROV_XXXXXX                             	| Yes                                                                 	|
| pop       	| Proof of possession.               	| POP value of the device like abcd1234   	| Optional. Considered empty string if not available in QR code data. 	|
| transport 	| Wi-Fi provisioning transport type. 	| It can be softap or ble.                	| Yes                                                                 	|
| security  	| Security for device communication. 	| It can be 0 or 1 int value.             	| Optional. Considered Sec1 if not available in QR code data.         	|
| password  	| Password of SoftAP device.         	| Password to connect with SoftAP device. 	| Optional                                                            	|

In provisioning library, there are two options for QR code scanning API. 
 

## License  
  

    Copyright 2020 Espressif Systems (Shanghai) PTE LTD  
     
    Licensed under the Apache License, Version 2.0 (the "License");  
    you may not use this file except in compliance with the License.  
    You may obtain a copy of the License at  
     
        http://www.apache.org/licenses/LICENSE-2.0  
     
    Unless required by applicable law or agreed to in writing, software  
    distributed under the License is distributed on an "AS IS" BASIS,  
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  
    See the License for the specific language governing permissions and  
    limitations under the License.
