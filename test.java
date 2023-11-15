import requests
from concurrent.futures import ThreadPoolExecutor, as_completed

def api_request(url):
    response = requests.get(url)
    # Process the response as needed
    return response.json()

# The API URL you want to call
api_url = "https://api.example.com/endpoint"

# Number of parallel requests
num_requests = 5

with ThreadPoolExecutor(max_workers=num_requests) as executor:
    # Using list comprehension to create a list of futures
    futures = [executor.submit(api_request, api_url) for _ in range(num_requests)]

    # Using as_completed to iterate over completed futures
    for future in as_completed(futures):
        try:
            result = future.result()
            # Process the result as needed
            print(result)
        except Exception as e:
            print(f"Error: {e}")
