# Lời nói đầu

Vì cái tools nào cũng cần cài CLI để chạy nhưng mà Nam không thích cài nhiều nó tùm lum cái máy nên chạy trong docker hết nha ae, mn muốn chạy nhớ phải có docker trước tiên nha, riêng sonarqube thì cần có thêm 1 server riêng nên Nam để cách setup ở dưới nha.

Vì tools nào scan ra cũng lỗi tùm lum hết á mn fix xong thì khùng luôn nên là cuối mỗi lệnh Nam có thêm "|| true" để nó lúc nào cũng pass, vì chủ yếu là để biết cách chạy tools thôi.

## Environments

```env
environment {
    SNYK_TOKEN = credentials('snyk-token-id')
    SONAR_TOKEN = credentials('sonarqube-token-id')
    SONAR_HOST_URL = credentials('sonarqube-host-url')
}
```

Cái này mn set trong credentials của Jenkins trên máy mn á, step thì hỏi AI nha (Nam cx hỏi AI thui), rồi lấy ra xài thôi, cách lấy token của snyk và sonar thì AI luôn nha mn, còn về sonar_host_url thì là đường dẫn tới server sonar chạy trên máy mn á (lưu ý là nên để ip private nha: 192.168.?.? vì để localhost mà chạy docker thì trong môi trường của docker nên nó không hiểu được localhost là máy mình).

## Gitleaks

Tool này chủ yếu là scan credentials có tồn tại trong commits (có nghĩa là nó scan tất cả commit có trong .git luôn).

Lệnh chạy trong docker:

```docker
docker run --rm \
    -v "${WORKSPACE}":/code \
    ghcr.io/gitleaks/gitleaks:latest \
    detect --source /code --verbose --redact --no-color || true
```

WORKSPACE là biến môi trường có sẵn của Jenkins trỏ tới thư mục làm việc hiện tại.

ghcr.io/gitleaks/gitleaks:latest: image docker có sẵn gitleaks CLI.

"${WORKSPACE}":/code: bind mount thư mục hiện tại vào docker.

detect --source /code: rồi trong docker detect thư mục code là xong

mấy cái cờ kia mn tự tìm hiểu nha (Nam cx ko hiểu đâu chủ yếu gen AI).

## Snyk

Tool này quét các dependencies rồi dò xem có lỗi không á, nó quét mấy cái file config như package.json, pom.xml, ... Thật ra nó có nhiều chế độ quét khác nữa mà trong đây thì quết dependencies thôi.

```docker
docker run --rm \
    -e SNYK_TOKEN=${SNYK_TOKEN} \
    -v "${WORKSPACE}":/project \
    -w /project \
    snyk/snyk:maven \
    snyk test --severity-threshold=high --all-projects || true
```

SNYK_TOKEN: này là token buộc phải có để chạy nha (có load lên ở phần environment giờ chỉ cần pass vào docker thôi).

snyk/snyk:maven: này là image của riêng maven, có image khác cho node, gradle, ... tùy project mà chọn. Nhưng mà không hiểu sao image của maven nó vẫn quết package.json của Node được mn (quá ảo) vì project mình monorepo có UI làm bằng nextjs nên có folder node.

--all-projects: này là quét toàn bộ project con nè, vì mình là monorepo.

## Sonarqube

Scan code quality.

Th này là rối nhất phải chạy 1 server sonar riêng rồi còn yêu cầu tạo volume để lưu data, tạo database cho nó nữa.

### Server sonar setup

Tạo volume

```docker
docker volume create --name sonarqube_data
docker volume create --name sonarqube_logs
docker volume create --name sonarqube_extensions
```

Tạo network (để database ns chuyện vs sonar)

```docker
docker network create sonarnet
```

Chạy database (ở đây Nam chọn postgre vì nó thân thuộc):

```docker
docker run -d --name sonar-postgres \
    --network sonarnet \
    -e POSTGRES_USER=sonar \
    -e POSTGRES_PASSWORD=sonar_secure_password \
    -e POSTGRES_DB=sonarqube \
    -v postgres_data:/var/lib/postgresql/data \
    postgres:15
```

Chạy sonarqube

```docker
docker run -d --name sonarqube \
    --network sonarnet \
    -p 9000:9000 \
    -e SONAR_JDBC_URL=jdbc:postgresql://sonar-postgres:5432/sonarqube \
    -e SONAR_JDBC_USERNAME=sonar \
    -e SONAR_JDBC_PASSWORD=sonar_secure_password \
    -v sonarqube_data:/opt/sonarqube/data \
    -v sonarqube_extensions:/opt/sonarqube/extensions \
    -v sonarqube_logs:/opt/sonarqube/logs \
    sonarqube:lts-community
```

### CI with sonarqube

Có server thì giờ chỉ cần chạy sonar CLI trong docker để communicate vs server đó là xong

```docker
docker run --rm \
    -e SONAR_HOST_URL=${SONAR_HOST_URL} \
    -e SONAR_TOKEN=${SONAR_TOKEN} \
    -v "${WORKSPACE}":/usr/src \
    sonarsource/sonar-scanner-cli \
    -Dsonar.projectKey=yas-monorepo \
    -Dsonar.sources=. \
    -Dsonar.java.binaries=**/target/classes \
    -Dsonar.exclusions=**/node_modules/**,**/.next/**,**/dist/**,**/postgres/**,**/.turbo/** || true
```

exclusions: bỏ qua các thư mục này không quét, thường là thư viện hoặc file được gen.
