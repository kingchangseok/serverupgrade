# ECAMS Agent 버그픽스 우선순위 가이드 (수정판)

> **업데이트**: P0-2 좀비 프로세스 항목 제거 (운영 검증 완료)  
> **최종 수정**: 2026-01-23

---

## 🚨 긴급 (P0) - 즉시 수정 필요 (1주 이내)

### P0-1. Fork 폭탄 DoS 취약점 ⚠️ CRITICAL

**위치**: `svrsrc/ecams_svr.c:397`

**문제**: 무제한 fork() → 시스템 리소스 고갈

**패치 코드**:
```c
#define MAX_CHILDREN 100
static volatile int active_children = 0;

while (1) {
    if (active_children >= MAX_CHILDREN) {
        usleep(100000);
        continue;
    }
    
    newsockfd = accept(sockfd, ...);
    pid = fork();
    
    if (pid > 0) {
        active_children++;
    }
}

// SIGCHLD 핸들러에서 카운터 감소
void sig_child(int sig) {
    int status;
    pid_t pid;
    while ((pid = wait3(&status, WNOHANG, NULL)) > 0) {
        active_children--;
    }
    signal(SIGCHLD, sig_child);
}
```

**영향도**: 높음  
**작업시간**: 2시간

---

### ~~P0-2. 좀비 프로세스 누적~~ ❌ 제거됨

**상태**: **FALSE POSITIVE - 패치 불필요**

**재분석 결과**:
- ✅ 실제 운영 환경에서 좀비 발생 안 함
- ✅ `ecams_svr.c:217`에서 `wait3()` 정상 호출 중
- ✅ 자식 프로세스 정상 회수 확인

**근거**:
```bash
# autoscan.log 증거
autoscan: warning: missing AC_FUNC_WAIT3 wanted by: 
	svrsrc/ecams_svr.c:217
```
→ 이는 "wait3()를 사용 중"이라는 의미 (configure.ac에 체크 누락일 뿐)

**자세한 분석**: `P0-2_ZOMBIE_REANALYSIS.md` 참조

---

### P0-2. select() 무한 대기 타임아웃 ⏰ (기존 P0-3)

**위치**: `svrsrc/lanapi.c:610`

**문제**: 타임아웃 없는 select() → 악의적 연결 시 무한 대기

**패치 코드**:
```c
struct timeval timeout;
timeout.tv_sec = 300;   // 5분
timeout.tv_usec = 0;

int ret = select(FD_SETSIZE, &readfds, NULL, NULL, &timeout);

if (ret == 0) {
    fprintf(stderr, "[WARN] select timeout (300s)\n");
    return -1;
}
```

**영향도**: 중  
**작업시간**: 30분

---

## ⚠️ 높음 (P1) - 1개월 내 수정 권장

### P1-1. AES 키 파일 권한 검증 누락 🔐

**위치**: `aessrc/aes.c`

**패치 코드**:
```c
#include <sys/stat.h>

int load_aes_key(const char *keyfile) {
    struct stat st;
    
    if (stat(keyfile, &st) < 0) {
        return -1;
    }
    
    // 권한 체크 (600 필수)
    if ((st.st_mode & 0777) != 0600) {
        fprintf(stderr, "[ERROR] Key file must have 0600 permission\n");
        return -1;
    }
    
    // 소유자 체크
    if (st.st_uid != 0 && st.st_uid != getuid()) {
        fprintf(stderr, "[ERROR] Key file must be owned by root or current user\n");
        return -1;
    }
    
    // 키 로딩...
}
```

**배포 시 주의**:
```bash
chmod 600 /etc/ecams/aes.key
chown root:root /etc/ecams/aes.key
```

**영향도**: 높음 (보안)  
**작업시간**: 2시간

---

### P1-2. 버퍼 오버플로 (경로 처리) 💥

**위치**: `tarsrc/extract.c`

**문제**: strcpy/strcat 사용 → 버퍼 오버플로 가능

**패치 코드**:
```c
char path[dfFullPath];
int len;

// strcpy/strcat 대신 snprintf 사용
len = snprintf(path, sizeof(path), "%s/%s", basedir, filename);

if (len >= sizeof(path)) {
    fprintf(stderr, "[ERROR] Path too long: %s/%s\n", basedir, filename);
    return -1;
}
```

**전체 수정 필요**:
```bash
grep -rn "strcpy\|strcat\|sprintf" tarsrc/
# → 모두 snprintf, strncat으로 교체
```

**영향도**: 높음 (보안)  
**작업시간**: 4시간

---

### P1-3. Path Traversal 취약점 🗂️

**위치**: `tarsrc/extract.c`

**패치 코드**:
```c
int validate_path(const char *path) {
    // ".." 검사
    if (strstr(path, "..") != NULL) {
        fprintf(stderr, "[ERROR] Path contains '..': %s\n", path);
        return -1;
    }
    
    // 절대 경로 금지
    if (path[0] == '/') {
        fprintf(stderr, "[ERROR] Absolute path not allowed: %s\n", path);
        return -1;
    }
    
    return 0;
}

// TAR 추출 시
if (validate_path(entry_name) < 0) {
    return -1;
}
```

**영향도**: 높음 (보안)  
**작업시간**: 3시간

---

### P1-4. SO_REUSEADDR 미설정 🔄

**위치**: `svrsrc/lanapi.c:418`

**패치 코드**:
```c
sockfd = socket(AF_INET, SOCK_STREAM, 0);

int opt = 1;
setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));

// bind, listen...
```

**영향도**: 중 (운영 편의성)  
**작업시간**: 30분

---

## 📊 중간 (P2) - 3개월 내 개선 권장

### P2-1. MD5 → SHA-256 교체 🔒

**이유**: MD5는 2004년부터 충돌 공격 가능

**작업시간**: 2주

---

### P2-2. 로그 시스템 부재 📝

**현재**: stderr만 사용

**권장**: syslog 또는 파일 로깅

**작업시간**: 1주

---

### P2-3. 설정 파일 하드코딩 ⚙️

**현재**: 포트, 타임아웃 하드코딩

**권장**: `/etc/ecams/ecams.conf` 도입

**작업시간**: 3일

---

## 📋 수정 요약

### P0 긴급 (1주 이내)

| 항목 | 상태 | 작업시간 |
|------|------|----------|
| P0-1. Fork 제한 | ✅ 유효 | 2시간 |
| ~~P0-2. 좀비 회수~~ | ❌ 제거 | N/A |
| P0-2. select() 타임아웃 | ✅ 유효 | 30분 |

**총 작업시간**: 2.5시간

### P1 높음 (1개월)

| 항목 | 작업시간 |
|------|----------|
| P1-1. AES 키 권한 | 2시간 |
| P1-2. 버퍼 오버플로 | 4시간 |
| P1-3. Path Traversal | 3시간 |
| P1-4. SO_REUSEADDR | 30분 |

**총 작업시간**: 9.5시간

---

## 🚀 빠른 적용

### Step 1: P0 긴급 패치 (2.5시간)

```bash
# 백업
cp ecams_svr.c ecams_svr.c.backup
cp lanapi.c lanapi.c.backup

# 패치 적용 (2개)
# - Fork 제한
# - select() 타임아웃

# 빌드 및 테스트
make clean && make
./test_p0.sh

# 배포
make install
systemctl restart ecams
```

### Step 2: P1 보안 패치 (1주)

```bash
# 4개 패치 적용
# 통합 테스트
# 카나리 배포 → 전체 배포
```

---

## 📞 변경 이력

**2026-01-23 v1.1**:
- ❌ P0-2 좀비 프로세스 항목 제거 (운영 검증 완료)
- ✅ P0-2로 select() 타임아웃 항목 재번호
- ✅ P0-2_ZOMBIE_REANALYSIS.md 추가

**2026-01-23 v1.0**:
- 초기 작성

---

**담당자**: Dev팀 리더  
**검증**: 운영팀 피드백 반영
