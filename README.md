# Resume Parser
Resume Parser is a REST API based service to parse basic informations from resume pdf. This project uses Apache PDFBox to parse the resume.

## Run Project
This is a spring-boot project. To run it we need to run following commands
```bash
mvn spring-boot:run
```

## REST API Documentation
### Parse a Resume
#### Request
To parse a resume need to follow this cURL request

```Shell
curl -XPOST -F file=@resume.pdf http://localhost:8080/resume/parse
```
#### Response
```
 HTTP/1.1 100 
 HTTP/1.1 200 
 Content-Type: application/json;charset=UTF-8
 Transfer-Encoding: chunked
 Date: Tue, 16 Apr 2019 10:15:47 GMT
```
```json 
{
  "name": "Name from resume",
  "email":"email@email.com",
  "phone":"+8801XXXXXXXXX",
  "image":"image-as-data-uri"
}
```

#### Dockerizing

```bash
mvn clean install
sudo docker build -t resume-parser:1.0 .
sudo docker run -d -p 8080:8080 resume-parser:1.0
```
