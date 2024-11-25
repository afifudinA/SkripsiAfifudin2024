import isOnline from 'is-online';
import shell from 'shelljs';
let CHECKINTERVAL = 10000;
var RASPIONLINE = true;

async function onlineCheck(){
    if(await isOnline()){
        if (!RASPIONLINE) {
            try {
                const restartResult = shell.exec('sudo systemctl restart boreClient.service') 
                if (restartResult.code === 0) {
                    console.log('boreClient.service restarted successfully.');
                } else {
                    console.error('Failed to restart boreClient.service');
                }
                console.log('Raspi is Online and ready!');
                console.log("BoreClient is restarted");
                
            } catch (error) {
                console.error("ERROR RESTARTING BORECLIENT SERVICE ", error.message);
            }
            
            RASPIONLINE = true;
        }
    }
    else{
        console.error('Raspi is offline');
        RASPIONLINE = false;
    }
};
setInterval(onlineCheck,CHECKINTERVAL);