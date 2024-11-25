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
const topic = process.env.TOPIC;
const bus = fs.readFileSync(process.env.BUS,'utf8');
const busObj = JSON.parse(bus);
const busStop = fs.readFileSync(process.env.BUS_STOP,'utf8');
const busStopObj = JSON.parse(busStop);
const rsuID = fs.readFileSync(process.env.NODE_ID,'utf8');
const rsuIDObj = JSON.parse(rsuID);
const API_getETA = process.env.API_GET_ETA;
const API_busInsertLocation = process.env.API_INSERT_BUS_LOCATION;
const tresholdHour = Number(process.env.TRESHOLD_HOUR);
const nodeID = rsuIDObj.nodeID;
const heartBeatInterval = process.env.HEARTBEAT_INTERVAL;
const autorestartPeriod = process.env.AUTORESTART_PERIOD;
const updateTopic = process.env.UPDATE_TOPIC;

const telemetryToken = busStopObj.telemetryToken[nodeID.toString()].toString();
const telemetryURL = process.env.API_TELEMETRY;

function run() {
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
    setInterval(heartBeat, heartBeatInterval);

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
    function heartBeat(){
        const msg2 = {
          message: 1
        };
        const json = JSON.stringify(msg2);
        if (client){
            client.publish(updateTopic, json, { qos: 0, retain: false }, (error) => {
                if (error){
                    console.log(error)
                }else{
                  console.log(`Send the telemetry to topic '${updateTopic}' : `, json);
                }
            })
        }
        // if(shell.exec('pm2 restart app.js').code !== 0){
        //   shell.exit(1);
        // };
      };
    
}
run();