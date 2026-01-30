#!/bin/bash
# build_all.sh - 환경점검 + 소스패치 + OS패치 + 빌드 통합 스크립트
#
# 사용법: ./build_all.sh [옵션]
#   옵션:
#     --skip-check      환경 점검 건너뛰기
#     --skip-patch      OS 패치 건너뛰기
#     --skip-autofix    소스 자동 패치 건너뛰기
#     --clean-only      clean만 수행
#     --help            도움말 출력

set -e  # 에러 시 즉시 중단

#===================================================================
# 변수 초기화
#===================================================================
SKIP_CHECK=false
SKIP_PATCH=false
SKIP_AUTOFIX=false
CLEAN_ONLY=false
BUILD_LOG="build_$(date +%Y%m%d_%H%M%S).log"

#===================================================================
# 옵션 파싱
#===================================================================
while [[ $# -gt 0 ]]; do
    case "$1" in
        --skip-check) SKIP_CHECK=true; shift ;;
        --skip-patch) SKIP_PATCH=true; shift ;;
        --skip-autofix) SKIP_AUTOFIX=true; shift ;;
        --clean-only) CLEAN_ONLY=true; shift ;;
        --help)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --skip-check      Skip build environment check"
            echo "  --skip-patch      Skip OS-specific patches"
            echo "  --skip-autofix    Skip automatic source code fixes"
            echo "  --clean-only      Only perform clean, skip build"
            echo "  --help            Show this help message"
            echo ""
            echo "Automatic fixes applied:"
            echo "  - Deprecated functions (bzero/bcopy -> memset/memcpy)"
            echo "  - BSD types for Alpine Linux (u_char, u_short, etc.)"
            echo "  - Network headers (lanapi.c)"
            exit 0
            ;;
        *) echo "Unknown option: $1"; exit 1 ;;
    esac
done

#===================================================================
# 로깅 함수
#===================================================================
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$BUILD_LOG"
}

log_error() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: $1" | tee -a "$BUILD_LOG"
}

log_success() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ✓ $1" | tee -a "$BUILD_LOG"
}

log_fail() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ✗ $1" | tee -a "$BUILD_LOG"
}

#===================================================================
# 0단계: 소스 코드 자동 패치 (auto_fix_source.sh 통합)
#===================================================================
auto_fix_source() {
    log "=========================================="
    log "0단계: 소스 코드 자동 패치"
    log "=========================================="
    log "자동 해결: Deprecated 함수, BSD 타입, 네트워크 헤더"
    log "수동 필요: 타입 정의, 중복 정의, Solaris statvfs, 링커 순서 등"
    log ""

    # 1. Deprecated 함수 치환 (bzero/bcopy → memset/memcpy)
    log "Step 1: Deprecated 함수 치환 (bzero/bcopy)..."
    REPLACED_COUNT=0
    
    while IFS= read -r file; do
        if grep -q "bzero\|bcopy" "$file" 2>/dev/null; then
            # 백업 생성
            cp "$file" "$file.autofix.bak"
            
            # 치환 수행
            sed -i 's/bzero(\([^,]*\),\s*\([^)]*\))/memset(\1, 0, \2)/g' "$file"
            sed -i 's/bcopy(\([^,]*\),\s*\([^,]*\),\s*\([^)]*\))/memcpy(\2, \1, \3)/g' "$file"
            
            log "  ✓ $file"
            REPLACED_COUNT=$((REPLACED_COUNT + 1))
        fi
    done < <(find . -type f \( -name "*.c" -o -name "*.h" \))
    
    log "  → $REPLACED_COUNT 파일 수정됨"

    # 2. BSD 타입 정의 추가 (u_char, u_short 등)
    log ""
    log "Step 2: BSD 타입 정의 추가 (Alpine Linux 대응)..."
    HEADER_FILE="inc/ecamsapi.h"
    
    if [ -f "$HEADER_FILE" ]; then
        if ! grep -q "typedef unsigned char u_char" "$HEADER_FILE"; then
            cp "$HEADER_FILE" "$HEADER_FILE.autofix.bak"
            
            # 임시 파일에 타입 정의 추가
            cat > "${HEADER_FILE}.tmp" << 'EOF'
/* BSD compatibility types for Alpine Linux / musl libc */
#ifndef u_char
typedef unsigned char u_char;
typedef unsigned short u_short;
typedef unsigned int u_int;
typedef unsigned long u_long;
#endif

EOF
            cat "$HEADER_FILE" >> "${HEADER_FILE}.tmp"
            mv "${HEADER_FILE}.tmp" "$HEADER_FILE"
            log_success "$HEADER_FILE - BSD 타입 추가"
        else
            log "  ○ $HEADER_FILE - BSD 타입 이미 존재"
        fi
    else
        log "  ✗ $HEADER_FILE 파일 없음 - SKIP"
    fi

    # 3. 네트워크 헤더 추가 (lanapi.c)
    log ""
    log "Step 3: 네트워크 헤더 추가 (lanapi.c)..."
    LANAPI_FILE="svrsrc/lanapi.c"
    
    if [ -f "$LANAPI_FILE" ]; then
        if ! grep -q "#include <sys/select.h>" "$LANAPI_FILE"; then
            cp "$LANAPI_FILE" "$LANAPI_FILE.autofix.bak"
            
            # sys/socket.h 다음에 헤더 추가
            sed -i '/#include <sys\/socket.h>/a\
#include <sys/select.h>\
#include <arpa/inet.h>\
#include <strings.h>    /* bzero, bcopy */
' "$LANAPI_FILE"
            log_success "$LANAPI_FILE - 헤더 추가"
        else
            log "  ○ $LANAPI_FILE - 헤더 이미 존재"
        fi
    else
        log "  ✗ $LANAPI_FILE 파일 없음 - SKIP"
    fi

    log ""
    log_success "소스 자동 패치 완료 (백업: *.autofix.bak)"
    log ""
    log "⚠️  다음 작업은 수동으로 진행 필요:"
    log "  - configure.ac 수정 (타입 정의, 함수 체크)"
    log "  - basename/th_read 조건부 컴파일"
    log "  - Solaris statvfs OS 분기 처리"
    log "  - Makefile.in 링커 순서 조정"
    log "  - 형변환/Format String 개별 수정"
}

#===================================================================
# 1단계: 환경 점검 (check_build_env.sh 통합)
#===================================================================
check_build_env() {
    log "=========================================="
    log "1단계: 빌드 환경 점검"
    log "=========================================="

    # OS 확인
    OS=$(uname -s)
    log "OS: $OS ($(uname -r))"

    # 컴파일러 확인
    if command -v gcc &> /dev/null; then
        log_success "GCC: $(gcc --version | head -n1)"
        export CC=gcc
    elif command -v cc &> /dev/null; then
        log_success "CC: $(cc -V 2>&1 | head -n1 || echo 'cc found')"
        export CC=cc
    else
        log_fail "C 컴파일러를 찾을 수 없습니다!"
        exit 1
    fi

    # 필수 헤더 확인
    HEADERS=("sys/socket.h" "netinet/in.h" "netdb.h" "sys/time.h" "arpa/inet.h")
    MISSING_HEADERS=()
    HEADER_CHECK_FAILED=0

    for header in "${HEADERS[@]}"; do
        if echo "#include <$header>" | $CC -E - &> /dev/null; then
            log_success "$header"
        else
            log_fail "$header NOT found"
            MISSING_HEADERS+=("$header")
            HEADER_CHECK_FAILED=1
        fi
    done

    # 누락된 헤더가 있는 경우에만 에러
    if [ "$HEADER_CHECK_FAILED" -eq 1 ]; then
        log_error "누락된 헤더: ${MISSING_HEADERS[*]}"
        log_error "개발 패키지 설치 필요 (예: build-essential, glibc-devel)"
        exit 1
    fi

    # autoconf 확인
    if command -v autoconf &> /dev/null; then
        log_success "Autoconf: $(autoconf --version | head -n1)"
    else
        log "WARNING: autoconf 미설치 (configure 파일이 있으면 무관)"
    fi

    log_success "환경 점검 완료"
}

#===================================================================
# 2단계: OS별 자동 패치 (autopatch.sh 통합)
#===================================================================
apply_os_patch() {
    log "=========================================="
    log "2단계: OS별 자동 패치"
    log "=========================================="

    OS=$(uname -s)

    case "$OS" in
        SunOS)
            log "Solaris 패치 적용 중..."

            # Makefile.in 수정 (네트워크 라이브러리)
            if [ -f Makefile.in ]; then
                if ! grep -q "\-lxnet" Makefile.in; then
                    sed -i 's/LIBS\s*=.*/LIBS = -lxnet -lnsl -lsocket -lresolv/' Makefile.in
                    log_success "Makefile.in에 Solaris 라이브러리 추가"
                fi
            fi

            # XPG4 PATH 우선
            export PATH=/usr/xpg4/bin:$PATH
            log_success "PATH에 /usr/xpg4/bin 추가"
            ;;

        AIX)
            log "AIX 패치 적용 중..."

            export CC="xlc -q32"
            export CFLAGS="-qlanglvl=extc99 -g"
            log_success "AIX xlc 32bit 옵션 설정"
            ;;

        HP-UX)
            log "HP-UX 패치 적용 중..."

            export CC="cc -Ae"
            export CFLAGS="-g +DA2.0W"
            export LIBS="-lxnet"
            log_success "HP-UX cc 옵션 설정"
            ;;

        Linux)
            log_success "Linux - 특별한 패치 불필요"
            ;;

        *)
            log "알 수 없는 OS: $OS (기본 설정 사용)"
            ;;
    esac

    log_success "OS 패치 완료"
}

#===================================================================
# 3단계: 빌드 수행 (build.sh 통합)
#===================================================================
do_build() {
    log "=========================================="
    log "3단계: 빌드 수행"
    log "=========================================="

    # 3-1. Clean
    log "이전 빌드 정리 중..."
    make clean 2>/dev/null || true
    rm -f config.cache config.status config.log
    log_success "Clean 완료"

    # Clean만 수행하는 경우 종료
    if [ "$CLEAN_ONLY" = true ]; then
        log_success "Clean only 모드 - 빌드 생략"
        return 0
    fi

    # 3-2. Configure
    if [ ! -f ./configure ]; then
        if command -v autoconf &> /dev/null; then
            log "configure 파일 생성 중..."
            autoconf
            log_success "autoconf 완료"
        else
            log_fail "configure 파일이 없고 autoconf도 설치되지 않음"
            exit 1
        fi
    fi

    log "configure 실행 중..."
    ./configure 2>&1 | tee -a "$BUILD_LOG"

    if [ ${PIPESTATUS[0]} -ne 0 ]; then
        log_fail "configure 실패 - config.log 확인 필요"
        exit 1
    fi
    log_success "configure 완료"

    # 3-3. Make
    log "컴파일 중..."
    make 2>&1 | tee -a "$BUILD_LOG"

    if [ ${PIPESTATUS[0]} -ne 0 ]; then
        log_fail "컴파일 실패"
        exit 1
    fi
    log_success "컴파일 완료"

    # 3-4. 빌드 결과 확인
    # Makefile에서 mv를 제거했으므로 현재 디렉토리 우선 확인
    INSTALL_PATHS=(
        "./ecams_svr"
        "../bin/ecams_svr"
        "../../bin/ecams_svr"
        "../../../bin/ecams_svr"
        "/SW2/polaris/bin/ecams_svr"
        "$(dirname $(pwd))/bin/ecams_svr"
    )

    FOUND_PATH=""
    for path in "${INSTALL_PATHS[@]}"; do
        if [ -f "$path" ]; then
            FOUND_PATH="$path"
            break
        fi
    done

    if [ -n "$FOUND_PATH" ]; then
        log_success "빌드 성공!"
        log "  파일: $FOUND_PATH"
        log "  크기: $(ls -lh "$FOUND_PATH" | awk '{print $5}')"
        log "  MD5:  $(md5sum "$FOUND_PATH" 2>/dev/null || md5 "$FOUND_PATH" 2>/dev/null || echo 'N/A')"
    else
        log_fail "ecams_svr 파일을 찾을 수 없음"
        log "  확인 경로: ${INSTALL_PATHS[*]}"
        exit 1
    fi
}

#===================================================================
# 메인 실행
#===================================================================
main() {
    log "=========================================="
    log "ECAMS Agent 통합 빌드 시작"
    log "=========================================="
    log "로그 파일: $BUILD_LOG"
    log ""

    # 0단계: 소스 자동 패치 (새로 추가)
    if [ "$SKIP_AUTOFIX" = false ]; then
        auto_fix_source
    else
        log "소스 자동 패치 건너뜀 (--skip-autofix)"
    fi

    # 1단계: 환경 점검
    if [ "$SKIP_CHECK" = false ]; then
        check_build_env
    else
        log "환경 점검 건너뜀 (--skip-check)"
    fi

    # 2단계: OS 패치
    if [ "$SKIP_PATCH" = false ]; then
        apply_os_patch
    else
        log "OS 패치 건너뜀 (--skip-patch)"
    fi

    # 3단계: 빌드
    do_build

    log "=========================================="
    log "ECAMS Agent 빌드 완료"
    log "=========================================="
    log ""
    log "로그 파일: $BUILD_LOG"
    log "백업 파일: *.autofix.bak"
}

# 스크립트 실행
main "$@"
