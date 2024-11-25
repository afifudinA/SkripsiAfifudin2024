import BeaconScanner from 'node-beacon-scanner';
const scanner = new BeaconScanner();
import axios from 'axios';
import mqtt from 'mqtt';
import fs from 'fs';
import dotenv from 'dotenv';
dotenv.config();
import isOnline from 'is-online';
import shell from 'shelljs';
// import { randomInt } from 'crypto';


const ca = fs.readFileSync(process.env.CA, 'utf8');
const cert = fs.readFileSync(process.env.CERT, 'utf8');
const key = fs.readFileSync(process.env.KEY, 'utf8');
const endpoint = process.env.ENDPOINT
const bus = fs.readFileSync(process.env.BUS,'utf8');
const busStop = fs.readFileSync(process.env.BUS_STOP,'utf8');
const busStopObj = JSON.parse(busStop);
const rsuID = fs.readFileSync(process.env.NODE_ID,'utf8');
const rsuIDObj = JSON.parse(rsuID);
const nodeID = rsuIDObj.nodeID;
const updateTopic = process.env.UPDATE_TOPIC;

const telemetryToken = busStopObj.telemetryToken[nodeID.toString()].toString();

console.log('telemetryToken: ', telemetryToken);


function run() {
    async function restartApp(){
        console.log('Restarting...')
        if (shell.exec('sudo systemctl restart boreClient.service').code !== 0){
          shell.exit(1);
        };
        if (shell.exec('pm2 restart all').code !== 0){
          shell.exit(1);
        };
      };

    async function updateApp(){
        // if (await shell.exec('git pull').code !== 0){
        //   shell.exit(1);
        // }
        console.log("App Updated")
        await restartApp();
      };
    
    const mqttOptions = {
        host: endpoint,
        protocol: "mqtt",
        clientId: "sdk-nodejs-v2",
        clean: true,
        key: key,
        cert: cert,
        ca: ca,
        reconnectPeriod: 0,
        debug:true
    };
    const client = mqtt.connect(mqttOptions);

  client.on('connect', function () {
      console.log("Connected to AWS IoT Core!");
      client.subscribe([updateTopic], () => {
        console.log(`Subscribe to topic '${updateTopic}'`);
      });
      // if (shell.exec('sudo systemctl restart boreClient.service').code !== 0){
      //   shell.exit(1);
      // };
  });


  client.on('message', (topic, payload) => {
    console.log('Receive Message: ', topic, payload.toString());
    const getData = JSON.parse(payload.toString());
    if (getData["message"] === 1){
      console.log('Update Data!!!');
      updateApp();
    }
  });

}

run();