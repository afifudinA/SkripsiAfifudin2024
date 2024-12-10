import requests
import json

# Define API URL
API_URL = "https://maps.open-street.com/api/route/"

# Parameters for the route
params = {
    "origin": "48.856614,2.3522219",  # Paris coordinates
    "destination": "45.764043,4.835659",  # Lyon coordinates
    "mode": "driving",  # Transportation mode
    "key": "cle-fournie"  # Your API key
}

# Send a GET request to the API
response = requests.get(API_URL, params=params)

# Parse the response
if response.status_code == 200:
    data = response.json()
    with open('osm_route.json', 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
    print("Route data saved to osm_route.json.")
else:
    print(f"API request failed with status code {response.status_code}.")
    print(f"Response: {response.text}")
