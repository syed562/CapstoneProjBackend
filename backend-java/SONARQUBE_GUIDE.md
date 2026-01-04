# SonarCloud Integration Guide

## Prerequisites
1. **Create SonarCloud Account**:
   - Go to: https://sonarcloud.io
   - Sign in with GitHub, Bitbucket, GitLab, or Azure DevOps
   - It's FREE for public repositories!

2. **Create Organization**:
   - After login, create a new organization or join existing one
   - Note your organization key (e.g., `your-org-name`)

3. **Import/Create Project**:
   - Click "+" → Analyze new project
   - Import from Git provider or create manually
   - Project key will be: `your-org-name_capstone-backend`

## Generate SonarCloud Token
1. Go to: https://sonarcloud.io/account/security
2. Generate Token:
   - Name: `capstone-backend`
   - Type: `Project Analysis Token` or `Global Analysis Token`
3. Copy the generated token (save it securely!)

## Configuration
Your project is already configured with:
- **Organization**: `syed562`
- **Project Key**: `syed562_CapstoneProjBackend`
- **Token**: Set as environment variable `SONAR_TOKEN`

## Running SonarCloud Analysis

### Option 1: Using Maven (Recommended)
```powershell
# Navigate to backend-java directory
cd C:\Users\syeds\Downloads\Capstone_Backend\backend-java

# Clean and test to generate coverage reports
mvn clean test

# Run SonarCloud analysis (token from environment variable SONAR_TOKEN)
mvn verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=syed562_CapstoneProjBackend
```

### Option 2: With Inline Configuration
```powershell
mvn clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar `
  -Dsonar.organization=syed562 `
  -Dsonar.projectKey=syed562_CapstoneProjBackend `
  -Dsonar.host.url=https://sonarcloud.io
```

### Option 3: Skip Tests (faster, no coverage)
```powershell
mvn sonar:sonar -Dsonar.token=YOUR_TOKEN_HERE -DskipTests
```

## What Gets Analyzed
- **Code Quality**: Bugs, vulnerabilities, code smells
- **Test Coverage**: JaCoCo coverage reports from all services
- **Duplications**: Duplicate code across services
- **Complexity**: Cyclomatic complexity metrics
- **Security**: Security hotspots and vulnerabilities
- **Reliability**: Bug detection and maintainability ratings

## Services Included
1. auth-service
2. loan-service
3. loan-application-service
4. profile-service
5. report-service
6. notification-service
7. api-gateway
8. eureka-server
9. config-server

## View Reports
After analysis completes:
1. Open https://sonarcloud.io
2. Navigate to your organization → capstone-backend project
3. View dashboards for:
   - Overview (Quality Gate status)
   - Issues (Bugs, Vulnerabilities, Code Smells)
   - Security Hotspots
   - Measures (Coverage, Duplications, Complexity)
   - Code (browse source with issues inline)
   - Activity (history of analyses)

## CI/CD Integration (GitHub Actions Example)
```yaml
name: SonarCloud Analysis

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  sonarcloud:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
        with:
          fetch-depth: 0  # Full history for better analysis
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Cache SonarCloud packages
        uses: actions/cache@v3
        with:
          path: ~/.sonar/cache
          key: ${{ runner.os }}-sonar
      
      - name: Build and analyze
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
        run: |
          cd backend-java
          mvn clean verify sonar:sonar \
            -Dsonar.organization=${{ secrets.SONAR_ORGANIZATION }} \
            -Dsonar.projectKey=${{ secrets.SONAR_PROJECT_KEY }}
```

Add these secrets to your GitHub repository:
- `SONAR_TOKEN`: Your SonarCloud token
- `SONAR_ORGANIZATION`: Your organization key
- `SONAR_PROJECT_KEY`: Your project key (e.g., `your-org_capstone-backend`)

## Quality Gate
SonarCloud will automatically check your code against quality gate conditions:
- Coverage > 80% (configurable)
- No new bugs/vulnerabilities
- Security rating A
- Maintainability rating A

You can customize quality gates in SonarCloud project settings.

## Badges
Add SonarCloud badges to your README.md:
```markdown
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=YOUR_PROJECT_KEY&metric=alert_status)](https://sonarcloud.io/dashboard?id=YOUR_PROJECT_KEY)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=YOUR_PROJECT_KEY&metric=coverage)](https://sonarcloud.io/dashboard?id=YOUR_PROJECT_KEY)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=YOUR_PROJECT_KEY&metric=bugs)](https://sonarcloud.io/dashboard?id=YOUR_PROJECT_KEY)
```

## Troubleshooting
- **Authentication failed**: Check your token is valid and not expired
- **Organization not found**: Verify organization key is correct in pom.xml
- **No coverage data**: Run `mvn clean test` before `sonar:sonar`
- **Memory issues**: Increase Maven memory: `set MAVEN_OPTS=-Xmx1024m`
- **Rate limiting**: SonarCloud has analysis limits for free tier

## Benefits of SonarCloud vs Local SonarQube
- ✅ No installation/maintenance required
- ✅ Automatic updates
- ✅ Free for public repositories
- ✅ Integrated with GitHub/GitLab/Bitbucket
- ✅ Pull request analysis and decorations
- ✅ Historical trend tracking
- ✅ Team collaboration features

## Configuration Files
- `pom.xml`: SonarCloud plugin and properties
- `sonar-project.properties`: Project-specific SonarCloud settings
