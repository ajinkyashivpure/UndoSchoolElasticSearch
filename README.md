# Course Search Engine

A Spring Boot application that provides a REST API for searching courses with Elasticsearch backend. Features include full-text search, filtering, pagination, sorting, autocomplete suggestions, and fuzzy matching.

## 🚀 Features

- **Full-text search** on course titles and descriptions
- **Advanced filtering** by category, type, age range, price range, and session date
- **Flexible sorting** by upcoming sessions, price (ascending/descending)
- **Pagination support** for large result sets
- **Autocomplete suggestions** for course titles
- **Fuzzy search** to handle typos in search queries
- **RESTful API** with comprehensive endpoints
- **Docker-based Elasticsearch** setup
- **Sample data loading** on application startup

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose
- Git

## 🛠️ Setup Instructions

### Step 1: Clone the Repository

```bash
git clone https://github.com/ajinkyashivpure/UndoSchoolElasticSearch

```

### Step 2: Start Elasticsearch

Start Elasticsearch using Docker Compose:

```bash
docker-compose up -d
```

Verify Elasticsearch is running:

```bash
curl http://localhost:9200
```

Expected response:
```json
{
  "name" : "...",
  "cluster_name" : "docker-cluster",
  "cluster_uuid" : "...",
  "version" : {
    "number" : "8.11.0",
    ...
  }
}
```

### Step 3: Prepare Sample Data

Create `src/main/resources/sample-courses.json` with your course data. Each course should have:

```json
{
  "id": "unique-id",
  "title": "Course Title",
  "description": "Course description",
  "category": "Science|Math|Art|etc",
  "type": "ONE_TIME|COURSE|CLUB",
  "gradeRange": "1st-3rd",
  "minAge": 6,
  "maxAge": 10,
  "price": 99.99,
  "nextSessionDate": "2025-08-15T10:00:00Z"
}
```

### Step 4: Build and Run the Application

```bash
# Build the application
mvn clean compile

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080` and automatically:
1. Connect to Elasticsearch
2. Create the course index
3. Load sample data from `sample-courses.json`

### Step 5: Verify Setup

Check application health:
```bash
curl http://localhost:8080/api/health
```

Test basic search:
```bash
curl "http://localhost:8080/api/search"
```

## 📚 API Documentation

### Search Courses

**Endpoint:** `GET /api/search`

**Parameters:**
- `q` (string, optional): Search keyword for title and description
- `minAge` (integer, optional): Minimum age filter
- `maxAge` (integer, optional): Maximum age filter
- `category` (string, optional): Course category filter
- `type` (string, optional): Course type (ONE_TIME, COURSE, CLUB)
- `minPrice` (decimal, optional): Minimum price filter
- `maxPrice` (decimal, optional): Maximum price filter
- `startDate` (ISO-8601, optional): Filter courses on or after this date
- `sort` (string, optional): Sort order (upcoming, priceAsc, priceDesc)
- `page` (integer, optional): Page number (default: 0)
- `size` (integer, optional): Page size (default: 10)

**Examples:**

Basic search:
```bash
curl "http://localhost:8080/api/search"
```

Search with keyword:
```bash
curl "http://localhost:8080/api/search?q=physics"
```

Filter by category and price:
```bash
curl "http://localhost:8080/api/search?category=Science&minPrice=100&maxPrice=500"
```

Search with age range:
```bash
curl "http://localhost:8080/api/search?minAge=12&maxAge=16"
```

Sort by price (ascending):
```bash
curl "http://localhost:8080/api/search?sort=priceAsc"
```

Pagination:
```bash
curl "http://localhost:8080/api/search?page=1&size=5"
```

Filter by session date:
```bash
curl "http://localhost:8080/api/search?startDate=2025-08-20T00:00:00Z"
```

Complex query:
```bash
curl "http://localhost:8080/api/search?q=math&category=Math&type=COURSE&minAge=14&sort=priceDesc&page=0&size=20"
```

**Response Format:**
```json
{
  "total": 25,
  "page": 0,
  "size": 10,
  "totalPages": 3,
  "courses": [
    {
      "id": "course-001",
      "title": "Introduction to Physics",
      "description": "Basic physics concepts...",
      "category": "Science",
      "type": "COURSE",
      "gradeRange": "9th-12th",
      "minAge": 14,
      "maxAge": 18,
      "price": 299.99,
      "nextSessionDate": "2025-08-15T10:00:00Z"
    }
  ]
}
```

### Autocomplete Suggestions

**Endpoint:** `GET /api/search`

**Parameters:**
- `suggest` (string, required): Partial course title (minimum 2 characters)

**Examples:**

Get suggestions for "phy":
```bash
curl "http://localhost:8080/api/search?suggest=phy"
```

**Response:**
```json
{
  "suggestions": [
    "Introduction to Physics",
    "Advanced Physics Lab",
    "Physics for Engineers"
  ]
}
```

### Administrative Endpoints

**Reindex Data:** `POST /api/admin/reindex`
```bash
curl -X POST "http://localhost:8080/api/admin/reindex"
```

**Health Check:** `GET /api/health`
```bash
curl "http://localhost:8080/api/health"
```

## 🔍 Testing Fuzzy Search

The application supports fuzzy matching for typos. Examples:

Search for "dinasours" (typo in "dinosaurs"):
```bash
curl "http://localhost:8080/api/search?q=dinasours"
```

Search for "mathmatics" (typo in "mathematics"):
```bash
curl "http://localhost:8080/api/search?q=mathmatics"
```

## 🐳 Docker Services

The `docker-compose.yml` includes:

- **Elasticsearch** (port 9200): Search engine
- **Kibana** (port 5601): Elasticsearch UI (optional)

Access Kibana at `http://localhost:5601` to explore your data visually.

## 📊 Sample Search Scenarios

### Scenario 1: Parent looking for science courses for teenager
```bash
curl "http://localhost:8080/api/search?category=Science&minAge=13&maxAge=17&sort=upcoming"
```

### Scenario 2: Finding affordable art courses
```bash
curl "http://localhost:8080/api/search?category=Art&maxPrice=150&sort=priceAsc"
```

### Scenario 3: Looking for one-time workshops this month
```bash
curl "http://localhost:8080/api/search?type=ONE_TIME&startDate=2025-08-01T00:00:00Z"
```

### Scenario 4: Fuzzy search with typo
```bash
curl "http://localhost:8080/api/search?q=robatics"
# Should still find "Robotics" courses
```

## 🔧 Configuration

Key configuration properties in `application.yml`:

```yaml
spring:
  elasticsearch:
    uris: http://localhost:9200
  
app:
  elasticsearch:
    index:
      courses: courses
  data:
    sample-file: sample-courses.json
```

## 🚨 Troubleshooting

### Elasticsearch Connection Issues
1. Ensure Docker is running: `docker ps`
2. Check Elasticsearch logs: `docker-compose logs elasticsearch`
3. Verify connectivity: `curl http://localhost:9200`

### Data Not Loading
1. Check application logs for errors
2. Verify `sample-courses.json` format
3. Use reindex endpoint: `POST /api/admin/reindex`

### Search Not Working
1. Confirm data is indexed: `curl "http://localhost:8080/api/search"`
2. Check Elasticsearch index: `curl http://localhost:9200/courses/_search`
3. Review application logs for errors

## 📝 Version Compatibility

This project uses carefully selected compatible versions:

- **Spring Boot**: 3.2.5
- **Elasticsearch**: 8.11.0
- **Java**: 17
- **Jackson**: Managed by Spring Boot (no conflicts)

## 🎯 Performance Notes

- Elasticsearch queries use filters for exact matches (better performance)
- Bulk indexing is used for sample data loading
- Proper field mappings optimize search performance
- Fuzzy search has auto fuzziness to balance accuracy and performance

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request
