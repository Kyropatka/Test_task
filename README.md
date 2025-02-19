Test Task Application:
-
This Spring Boot application processes large product and trade files asynchronously, enriches trade data by matching product IDs with product names stored in Redis, and exposes a REST API to generate enriched trade CSV files.


I completed all 4 tasks using those libraries and APIs that are more familiar to me.


1. How to Run the Project:

     - Clone the Repository.
     - Clone the project repository to your local machine.

2. Configure Redis:

     - Make sure Redis is installed and running on localhost at port 6379.
     - You can update the connection settings in the application.properties file if needed.

3. Build the Project:

     - Use Maven (or your preferred build tool) to build the project.

4. Run the Application:

     - Run the application using your IDE.
     - On startup, the application loads product data from largeSizeProduct.csv (located in the resources) and saves the product names into Redis..
  
5. API Usage Instructions:

     - The application exposes a REST endpoint to enrich trade data.
       
6. Endpoint:

     - POST /api/v1/enrich
  
7. Request:

     - Parameter: file (MultipartFile): The file containing trade data. The file extension (e.g., csv, json, or xml) is used to determine the file format automatically.
  
8. Response:

     - A downloadable CSV file containing the enriched trades. The CSV includes the following columns: date, productName, currency, price
  
9. Example using curl:
   
     - curl -X POST -F "file=@/path/to/your/tradeFile.csv" http://localhost:8080/api/v1/enrich --output enriched_trades.csv
  
10. If I had more time, I would try to understand reactive programming and Reactor’s Flux. And I would try to implement reactive data processing.

11. Screenshots of testing:

Asynchrony and different input data formats
![image](https://github.com/user-attachments/assets/4e64b35a-677f-40b8-8f4d-6829a27bc30d)
![image](https://github.com/user-attachments/assets/52ae4143-a413-467c-a3aa-b4eaec7bca7c)

Stream API
![image](https://github.com/user-attachments/assets/940345cb-f4b3-4099-a1c2-e046023dc429)
![image](https://github.com/user-attachments/assets/708190cb-ae7e-49d1-8a04-5c9a4a0d41c5)

Test data set Trade.csv

![image](https://github.com/user-attachments/assets/f54c337a-2acc-40bf-8b83-97d893da9dc1)

Endpoint response.

![image](https://github.com/user-attachments/assets/83e3f0c3-85d8-4c80-8bf6-b16710faec81)

Endpoint response for test_trade.json

![image](https://github.com/user-attachments/assets/e2a8d736-e946-40a3-97ee-c3180743e7f9)

Endpoint response for test_trade.xml

![image](https://github.com/user-attachments/assets/9e3b44bc-855c-400d-8071-4bd0bf5cbced)

Response time and result of Stream API + Async to the middleSizeTrade file. (test 1 - 38.42s, test 2 - 38.06s, test 3 - 38.69s)

![image](https://github.com/user-attachments/assets/6a390d40-54be-43f8-9997-bc6e2bbece28)

Response time and result of Reactor's Flux to the middleSizeTrade file. (test 1 - 1m 20.78s, test 2 - 1m 29.28s, test 3 - 1m 28.86s)

![image](https://github.com/user-attachments/assets/067a329b-9a2f-4a88-8ea5-7ef57f9a7e52)



