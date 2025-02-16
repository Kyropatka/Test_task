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


