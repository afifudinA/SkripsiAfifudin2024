import fs from 'fs';
import dotenv from 'dotenv';
dotenv.config();

// import { randomInt } from 'crypto';

const rsuID = fs.readFileSync(process.env.NODE_ID,'utf8');
const rsuIDObj = JSON.parse(rsuID);
const nodeID = rsuIDObj.nodeID;


import { exec } from 'child_process';
// Replace the following command with the actual command to start your service
var maincommand = 'bore local 22 --to 54.169.36.133 --port 900';
var startCommand = maincommand + nodeID.toString();

console.log(startCommand);

exec(startCommand, (error, stdout, stderr) => {
  if (error) {
    console.error(`Error starting the service: ${error}`);
    return;
  }
  console.log(`Service started successfully:\n${stdout}`);
});
