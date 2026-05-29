---
type: Note
---
# Hướng dẫn cài đặt môi trường phát triển

**Stack:** Spring Boot · Spring JDBC Template · Thymeleaf · MySQL · Maven · Lombok

***

## 1. Yêu cầu phần mềm

| Phần mềm      | Phiên bản khuyến nghị   | Tải về                                                                                                                                                                                                       |
| ------------- | ----------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| JDK           | 21 (LTS)                | <https://adoptium.net/download?link=https%3A%2F%2Fgithub.com%2Fadoptium%2Ftemurin21-binaries%2Freleases%2Fdownload%2Fjdk-21.0.11%252B10%2FOpenJDK21U-jdk_x64_windows_hotspot_21.0.11_10.msi&vendor=Adoptium> |
| Maven         | 3.9.16                  | <https://anhtester.com/blog/apache-maven-la-gi-cai-dat-moi-truong-maven-tren-windows-b664.html> <https://dlcdn.apache.org/maven/maven-3/3.9.16/binaries/apache-maven-3.9.16-bin.zip>                         |
| MySQL         | 9.7.0 (LTS)             | <https://dev.mysql.com/downloads/mysql>                                                                                                                                                                      |
| IntelliJ IDEA | Community hoặc Ultimate | <https://www.jetbrains.com/idea/download>                                                                                                                                                                    |

Kiểm tra sau khi cài:

```bash
➜ java -version
openjdk version "21.0.11" 2026-04-21 LTS
OpenJDK Runtime Environment Temurin-21.0.11+10 (build 21.0.11+10-LTS)
OpenJDK 64-Bit Server VM Temurin-21.0.11+10 (build 21.0.11+10-LTS, mixed mode, sharing)

➜ mvn -version
Apache Maven 3.9.16 (2bdd9fddda4b155ebf8000e807eb73fd829a51d5)
Maven home: C:\apache-maven-3.9.16
Java version: 21.0.11, vendor: Eclipse Adoptium, runtime: C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot
Default locale: en_US, platform encoding: UTF-8
OS name: "windows 11", version: "10.0", arch: "amd64", family: "windows"

➜ mysql --version
C:\Program Files\MySQL\MySQL Server 9.7\bin\mysql.exe  Ver 9.7.0 for Win64 on x86_64 (MySQL Community Server - GPL)
```

***

## 2. Tạo project Spring Boot

Dùng Spring Initializr tại **<https://start.spring.io>** với cấu hình:

| Trường      | Giá trị |
| ----------- | ------- |
| Project     | Maven   |
| Language    | Java    |
| Spring Boot | 4.0.6   |
| Group       | hcmuaf  |
| Artifact    | `sad`   |
| Packaging   | Jar     |
| Java        | 21      |

Thêm các dependency sau (ô Search bên phải):

- `Spring Web` — Spring MVC, DispatcherServlet
- `Thymeleaf` — template engine cho View
- `JDBC API` — Spring JDBC Template (không phải JPA)
- `MySQL Driver` — driver kết nối MySQL
- `Lombok` — tự động sinh getter/setter/constructor lúc compile

Bấm **Generate** → tải file zip → giải nén → mở bằng IntelliJ IDEA.

> **Lưu ý IntelliJ IDEA:** Sau khi mở project, vào **File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors** → tích **Enable annotation processing**. Nếu không bật, Lombok sẽ không hoạt động.

***

## 3. Cấu trúc thư mục project

```text
shop/
├── src/
│   ├── main/
│   │   ├── java/com/yourname/shop/
│   │   │   ├── controller/          # @Controller — nhận request, gọi Model
│   │   │   ├── model/               # Entity + SQL trực tiếp trong đây
│   │   │   ├── dto/                 # Data bag gửi ra View hoặc nhận từ form
│   │   │   ├── mapper/              # Chuyển đổi Model ↔ DTO
│   │   │   └── ShopApplication.java
│   │   └── resources/
│   │       ├── templates/           # File .html Thymeleaf
│   │       ├── static/              # CSS, JS, ảnh tĩnh
│   │       └── application.properties
│   └── test/
└── pom.xml
```

***

## 4. Cấu hình `application.properties`

Mở file `src/main/resources/application.properties`, thêm:

```properties
# Application Properties
spring.application.name=pet-store

# Server
server.port=8080

# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/pet_store?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=2026
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Thymeleaf
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html
spring.thymeleaf.cache=false

# Log SQL ra console
logging.level.org.springframework.jdbc.core=DEBUG
```

***

## 5. Tạo database MySQL

Mở MySQL client (MySQL Workbench hoặc terminal):

```sql
CREATE DATABASE pet_store
```
