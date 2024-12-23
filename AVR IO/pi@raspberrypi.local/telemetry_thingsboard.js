import BeaconScanner from 'node-beacon-scanner';
import axios from 'axios';
import fs from 'fs';
import dotenv from 'dotenv';
import shell from 'shelljs';
dotenv.config();


const bus = fs.readFileSync(process.env.BUS,'utf8');
const busStop = fs.readFileSync(process.env.BUS_STOP,'utf8');
const busStopObj = JSON.parse(busStop);
const rsuID = fs.readFileSync(process.env.NODE_ID,'utf8');
const rsuIDObj = JSON.parse(rsuID);
const nodeID = rsuIDObj.nodeID;
const heartBeatInterval = process.env.HEARTBEAT_INTERVAL;


const telemetryToken = busStopObj.telemetryToken[nodeID.toString()].toString();
const telemetryURL = process.env.API_TELEMETRY;


function run(){
    async function sendTelemetry(telemetryData){
        console.log('telemetryToken: ', telemetryToken);
        if(telemetryToken === "-"){
          console.log('Telemetry data is not sent -> RSU does not has ACCESS_TOKEN!')
        }
        else{
          const API_telemetry = telemetryURL.replace("$ACCESS_TOKEN", telemetryToken); 
          const options = {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json'
            }
          };
          try {
            const response = await axios.post(API_telemetry, telemetryData, options);
            console.log('Send telemetry data to thingsboard: ', telemetryData);
          } catch (error) {
            console.log(error);
            const result = shell.exec('pm2 restart all');
            if (result.code !== 0) {
              console.log('Failed to restart process:', result.stderr);
            } else {
              console.log('Process restarted successfully');
            }
          }
        }
      }

    var temp = fs.readFileSync("/sys/class/thermal/thermal_zone0/temp");
    var temp_c = temp/1000;

    // var temp_c = randomInt(25, 40);

    const date = new Date();
    var sampleTime = date.getTime();

    const msg1 = {
      internet: 1,
      node: 1, 
      temperature: temp_c
    };
    const telemetryData = JSON.stringify(msg1);
    sendTelemetry(telemetryData);
}

setInterval(run, heartBeatInterval);
