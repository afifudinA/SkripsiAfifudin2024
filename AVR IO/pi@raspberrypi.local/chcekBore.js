import { exec } from 'child_process';

const CHECK_INTERVAL = 60000; // 1 minute
const RESTART_INTERVAL = 300000; // 5 minutes

const serviceStatusCommand = 'systemctl status boreClient.service';
const reloadCommand = 'sudo systemctl start boreClient.service';
const restartCommand = 'sudo systemctl restart boreClient.service';

function checkBore() {
  exec(serviceStatusCommand, (error, stdout, stderr) => {
    if (error) {
      exec(reloadCommand, (error, stdout) => {
        if (error) {
          console.log(`Failed to reload BoreClient, Error: ${error}`);
          return;
        }
        console.log(`Success to start BoreClient: ${stdout}`);
      });
      return;
    }
    console.log(`Service status:\n${stdout}`);
  });
}

function restartBore() {
  exec(restartCommand, (error, stdout, stderr) => {
    if (error) {
      console.log(`Failed to restart BoreClient, Error: ${error}`);
      return;
    }
    console.log(`Successfully restarted BoreClient: ${stdout}`);
  });
}

setInterval(checkBore, CHECK_INTERVAL);
setInterval(restartBore, RESTART_INTERVAL);
