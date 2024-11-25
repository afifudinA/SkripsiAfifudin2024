scanner.onadvertisement = (ad) => {
    scanner.stopScan();
    console.log(ad);
    const scannedBLE = ad["id"];
    const bleAddress = ad["bleAddress"];
    const proximityUUID = ad["iBeacon"]["uuid"];
    const rssi = ad["rssi"];
    const txPower = ad["iBeacon"]["txPower"];

    if (Object.keys(busObj.bleID).includes(scannedBLE)){
      console.log('iBeacon of the bus is found!')
      checkAndSendData(scannedBLE, bleAddress, proximityUUID, rssi, txPower);
    }
    else console.log('Beacon detected, but not the Bus!')
  };

  // Start scanning
  scanner.startScan().then(() => {
      console.log('Started to scan.');
  }).catch((error) => {
      console.error(error);
  });