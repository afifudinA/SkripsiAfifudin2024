const fs = require('fs');
const csv = require('csv-parser');

const results = [];

fs.createReadStream('coordinate.csv')
  .pipe(csv())
  .on('data', (data) => {
    // Remove any single quotes and parse the latitude and longitude
    const lat = parseFloat(data.latitude);
    const lng = parseFloat(data.longitude);
    results.push({ lat, lng });
  })
  .on('end', () => {
    const output = results.map(coord => `  { lat: ${coord.lat}, lng: ${coord.lng} },`).join('\n');
    const outputContent = `const flightPlanCoordinates = [\n${output}\n];`;

    fs.writeFile('output.txt', outputContent, (err) => {
      if (err) throw err;
      console.log('Output has been written to output.txt');
    });
  });


  