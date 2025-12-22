## Redis Test Application

A complete Spring Boot application for testing Redis operations with a web interface.

### Features

- ✅ **String Operations**: Set, get, TTL support
- ✅ **Object Operations**: JSON object storage and retrieval  
- ✅ **Key Management**: List, delete, check existence, TTL
- ✅ **Hash Operations**: Field-based storage
- ✅ **List Operations**: Push, pop, range queries
- ✅ **Utility Operations**: Counters, increment/decrement
- ✅ **Web Interface**: Beautiful HTML interface for testing
- ✅ **REST API**: Complete RESTful endpoints
- ✅ **Redis Configuration**: Configured for your Redis instance

### Configuration

The application is pre-configured for your Redis instance:
- **Host**: localhost.com
- **Port**: 10000  
- **Username**: default
- **Password**: saf5y4hghr
- **Database**: 0

### Quick Start

1. **Run the application**:
   ```bash
   cd /workspaces/java-webview/TestRedis
   ./gradlew bootRun
   ```

2. **Access the web interface**:
   ```
   http://localhost:8081/redis-app/
   ```

3. **Test the API**:
   ```bash
   # Set a string
   curl -X POST "http://localhost:8081/redis-app/api/redis/string?key=test&value=hello"
   
   # Get a string
   curl "http://localhost:8081/redis-app/api/redis/string/test"
   
   # Get Redis info
   curl "http://localhost:8081/redis-app/api/redis/info"
   ```

### API Endpoints

#### String Operations
- `POST /api/redis/string?key={key}&value={value}&ttl={seconds}` - Set string
- `GET /api/redis/string/{key}` - Get string

#### Object Operations  
- `POST /api/redis/object?key={key}&ttl={seconds}` - Set object (JSON body)
- `GET /api/redis/object/{key}` - Get object

#### Key Operations
- `GET /api/redis/keys?pattern={pattern}` - List keys
- `DELETE /api/redis/key/{key}` - Delete key
- `GET /api/redis/key/{key}/exists` - Check if key exists
- `GET /api/redis/key/{key}/ttl` - Get key TTL

#### Hash Operations
- `POST /api/redis/hash/{key}?field={field}` - Set hash field (JSON body)
- `GET /api/redis/hash/{key}/{field}` - Get hash field
- `GET /api/redis/hash/{key}` - Get all hash fields

#### List Operations
- `POST /api/redis/list/{key}/push?direction={left|right}` - Push to list (JSON body)
- `POST /api/redis/list/{key}/pop?direction={left|right}` - Pop from list
- `GET /api/redis/list/{key}?start={start}&end={end}` - Get list range

#### Utility Operations
- `POST /api/redis/increment/{key}?delta={number}` - Increment counter
- `GET /api/redis/info` - Get Redis connection info

### Project Structure

```
TestRedis/
├── src/main/java/com/example/redis/
│   ├── RedisTestApplication.java     # Main application
│   ├── config/
│   │   └── RedisConfig.java          # Redis configuration
│   ├── controller/
│   │   └── RedisController.java      # REST API endpoints
│   └── service/
│       └── RedisService.java         # Redis operations service
├── src/main/resources/
│   ├── application.yml               # Application configuration
│   └── static/
│       └── index.html                # Web testing interface
└── build.gradle                      # Dependencies and build config
```

### Troubleshooting

If you get authentication errors:
1. Check Redis is running on `localhost.com:10000`
2. Verify credentials: username=`default`, password=`saf5y4hghr`
3. Test connection: `redis-cli -h localhost.com -p 10000 -a saf5y4hghr --user default`

### Development

- **Spring Boot 3.2.1** with Java 17
- **Lettuce** Redis client (with Jedis backup)
- **RESTful API** with proper error handling
- **Responsive web interface** for testing
- **Health checks** via Spring Actuator